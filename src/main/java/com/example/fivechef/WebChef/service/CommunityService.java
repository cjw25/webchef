package com.example.fivechef.WebChef.service;

import com.example.fivechef.WebChef.dto.CommunityCreateRequest;
import com.example.fivechef.WebChef.dto.CommunityResponse;
import com.example.fivechef.WebChef.dto.CommunityUpdateRequest;
import com.example.fivechef.WebChef.entity.Community;
import com.example.fivechef.WebChef.entity.Role;
import com.example.fivechef.WebChef.entity.User;
import com.example.fivechef.WebChef.repository.CommunityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class CommunityService {

    private static final List<String> ALLOWED_EXTENSIONS =
            List.of("jpg", "jpeg", "png", "gif", "webp");

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    private final CommunityRepository communityRepository;

    private final UserService userService;

    @Value("${file.community-upload-dir:uploads/community}")
    private String uploadDir;

    @Transactional(readOnly = true)
    public Community getCommunityEntity(Long id) {
        return communityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public Page<CommunityResponse> getCommunities(int page, String keyword, String category) {
        Pageable pageable = PageRequest.of(
                page,
                10,
                Sort.by(Sort.Order.desc("id"))
        );

        if (isBlank(keyword)) {
            return communityRepository.findAll(pageable)
                    .map(CommunityResponse::new);
        }

        String kw = keyword.trim();

        return communityRepository.findBySubjectContainingOrContentContaining(
                        kw,
                        kw,
                        pageable
                )
                .map(CommunityResponse::new);
    }

    @Transactional(readOnly = true)
    public CommunityResponse getCommunityResponse(Long id) {
        Community community = getCommunityEntity(id);
        return new CommunityResponse(community, true);
    }

    @Transactional
    public void createCommunity(CommunityCreateRequest request,
                                String username,
                                MultipartFile[] image) {
        validateCreateRequest(request);

        MultipartFile uploadFile = extractSingleFile(image);
        validateFile(uploadFile);

        User author = userService.getLoginUserEntity(username);

        Community community = new Community();
        community.setCategory(request.getCategory());
        community.setSubject(request.getSubject().trim());
        community.setContent(request.getContent().trim());
        community.setAuthor(author);

        saveFileIfExists(community, uploadFile);

        communityRepository.save(community);
    }

    @Transactional
    public void updateCommunity(Long id,
                                CommunityUpdateRequest request,
                                String username) {
        updateCommunity(id, request, username, null, false);
    }

    @Transactional
    public void updateCommunity(Long id,
                                CommunityUpdateRequest request,
                                String username,
                                MultipartFile[] image,
                                boolean deleteFile) {
        validateUpdateRequest(request);

        Community community = getCommunityEntity(id);
        User loginUser = userService.getLoginUserEntity(username);

        checkOwnerOrAdmin(community, loginUser, "수정 권한이 없습니다.");

        community.setCategory(request.getCategory());
        community.setSubject(request.getSubject().trim());
        community.setContent(request.getContent().trim());

        MultipartFile uploadFile = extractSingleFile(image);
        validateFile(uploadFile);

        if (deleteFile) {
            deleteStoredFile(community);
            clearFileInfo(community);
        }

        if (uploadFile != null && !uploadFile.isEmpty()) {
            deleteStoredFile(community);
            clearFileInfo(community);
            saveFileIfExists(community, uploadFile);
        }

        communityRepository.save(community);
    }

    @Transactional
    public void deleteCommunity(Long id, String username) {
        Community community = getCommunityEntity(id);
        User loginUser = userService.getLoginUserEntity(username);

        checkOwnerOrAdmin(community, loginUser, "삭제 권한이 없습니다.");

        deleteStoredFile(community);

        communityRepository.delete(community);
    }

    @Transactional
    public void voteCommunity(Long id, String username) {
        Community community = getCommunityEntity(id);
        User loginUser = userService.getLoginUserEntity(username);

        boolean alreadyVoted = community.getVoter()
                .removeIf(user -> user.getId().equals(loginUser.getId()));

        if (!alreadyVoted) {
            community.getVoter().add(loginUser);
        }

        communityRepository.save(community);
    }

    private MultipartFile extractSingleFile(MultipartFile[] image) {
        if (image == null || image.length == 0) {
            return null;
        }

        MultipartFile selectedFile = null;
        int fileCount = 0;

        for (MultipartFile file : image) {
            if (file == null || file.isEmpty()) {
                continue;
            }

            fileCount++;
            selectedFile = file;
        }

        if (fileCount > 1) {
            throw new IllegalArgumentException("현재 첨부파일은 1개만 업로드할 수 있습니다.");
        }

        return selectedFile;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return;
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 용량은 10MB를 초과할 수 없습니다.");
        }

        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null || originalFileName.isBlank()) {
            throw new IllegalArgumentException("잘못된 파일입니다.");
        }

        String extension = getExtension(originalFileName).toLowerCase();

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("이미지 파일(jpg, jpeg, png, gif, webp)만 업로드할 수 있습니다.");
        }
    }

    private void saveFileIfExists(Community community, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return;
        }

        try {
            String originalFileName = file.getOriginalFilename();

            if (originalFileName == null || originalFileName.isBlank()) {
                return;
            }

            String extension = getExtension(originalFileName).toLowerCase();
            String storedFileName = UUID.randomUUID() + "." + extension;

            Path uploadPath = Paths.get(uploadDir)
                    .toAbsolutePath()
                    .normalize();

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(storedFileName);

            file.transferTo(filePath.toFile());

            community.setOriginalFileName(originalFileName);
            community.setStoredFileName(storedFileName);
            community.setFileUrl("/uploads/community/" + storedFileName);

        } catch (Exception e) {
            throw new IllegalArgumentException("파일 업로드 중 오류가 발생했습니다.");
        }
    }

    private void deleteStoredFile(Community community) {
        if (isBlank(community.getStoredFileName())) {
            return;
        }

        try {
            Path filePath = Paths.get(uploadDir)
                    .toAbsolutePath()
                    .normalize()
                    .resolve(community.getStoredFileName());

            Files.deleteIfExists(filePath);

        } catch (Exception e) {
            throw new IllegalArgumentException("기존 파일 삭제 중 오류가 발생했습니다.");
        }
    }

    private void clearFileInfo(Community community) {
        community.setOriginalFileName(null);
        community.setStoredFileName(null);
        community.setFileUrl(null);
    }

    private String getExtension(String filename) {
        int dotIdx = filename.lastIndexOf(".");

        if (dotIdx == -1 || dotIdx == filename.length() - 1) {
            throw new IllegalArgumentException("확장자가 없는 파일은 업로드할 수 없습니다.");
        }

        return filename.substring(dotIdx + 1);
    }

    private void validateCreateRequest(CommunityCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("게시글 등록 정보가 없습니다.");
        }

        if (isBlank(request.getSubject())) {
            throw new IllegalArgumentException("제목을 입력해주세요.");
        }

        if (isBlank(request.getContent())) {
            throw new IllegalArgumentException("내용을 입력해주세요.");
        }
    }

    private void validateUpdateRequest(CommunityUpdateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("게시글 수정 정보가 없습니다.");
        }

        if (isBlank(request.getSubject())) {
            throw new IllegalArgumentException("제목을 입력해주세요.");
        }

        if (isBlank(request.getContent())) {
            throw new IllegalArgumentException("내용을 입력해주세요.");
        }
    }

    private void checkOwnerOrAdmin(Community community, User loginUser, String message) {
        boolean isAdmin = loginUser.getRole() == Role.ADMIN;

        boolean isOwner = community.getAuthor() != null
                && community.getAuthor().getId().equals(loginUser.getId());

        if (!isAdmin && !isOwner) {
            throw new IllegalArgumentException(message);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}