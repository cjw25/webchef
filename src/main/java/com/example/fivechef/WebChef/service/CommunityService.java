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
import com.example.fivechef.WebChef.entity.CommunityImage;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class CommunityService {

    private static final List<String> ALLOWED_EXTENSIONS =
            List.of("jpg", "jpeg", "png", "gif", "webp");

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final int MAX_FILE_COUNT = 5;

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
    public Page<CommunityResponse> getCommunities(int page, String keyword, String category, String currentUsername) {
        Pageable pageable = PageRequest.of(
                page,
                10,
                Sort.by(Sort.Order.desc("id"))
        );

        boolean hasCategory = !isBlank(category);
        boolean hasKeyword = !isBlank(keyword);

        if (!hasCategory && !hasKeyword) {
            return communityRepository.findAll(pageable)
                    .map(community -> new CommunityResponse(community, false, currentUsername));
        }

        if (hasCategory && !hasKeyword){
            return communityRepository.findByCategory(category, pageable)
                    .map(community -> new CommunityResponse(community, false, currentUsername));
        }

        String kw = keyword.trim();

        if (!hasCategory){
            return communityRepository.findBySubjectContainingOrContentContaining(kw, kw, pageable)
                    .map(community -> new CommunityResponse(community, false, currentUsername));
        }

        return communityRepository.findByCategoryAndSubjectContainingOrCategoryAndContentContaining(
                        category,
                        kw,
                        category,
                        kw,
                        pageable
                )
                .map(community -> new CommunityResponse(community, false, currentUsername));
    }

    @Transactional(readOnly = true)
    public CommunityResponse getCommunityResponse(Long id){
        Community community = getCommunityEntity(id);
        return new CommunityResponse(community, true);
    }

    @Transactional
    public CommunityResponse getCommunityResponse(Long id, String currentUsername) {
        Community community = getCommunityEntity(id);
        community.setViewCount(community.getViewCount() + 1);
        return new CommunityResponse(community, true, currentUsername);
    }

    @Transactional
    public void createCommunity(CommunityCreateRequest request,
                                String username,
                                MultipartFile[] img,
                                int mainIndex) {
        validateCreateRequest(request);

        User author = userService.getLoginUserEntity(username);

        Community community = new Community();
        community.setCategory(request.getCategory());
        community.setSubject(request.getSubject().trim());
        community.setContent(request.getContent().trim());
        community.setAuthor(author);

        communityRepository.save(community);

        saveImages(community, img, mainIndex);
    }

    @Transactional
    public void updateCommunity(Long id,
                                CommunityUpdateRequest request,
                                String username,
                                MultipartFile[] img
                                ) {
        validateUpdateRequest(request);

        Community community = getCommunityEntity(id);
        User loginUser = userService.getLoginUserEntity(username);

        checkOwnerOrAdmin(community, loginUser, "수정 권한이 없습니다.");

        community.setCategory(request.getCategory());
        community.setSubject(request.getSubject().trim());
        community.setContent(request.getContent().trim());

        removeImages(community, request.getDeleteImageIds());

        List<MultipartFile>  validFiles = extractValidFiles(img);
        List<CommunityImage> newImages = appendImages(community, validFiles);

        applyMainSelection(community, request.getMainSelect(), newImages);

        communityRepository.save(community);
    }

    @Transactional
    public void deleteCommunity(Long id, String username) {
        Community community = getCommunityEntity(id);
        User loginUser = userService.getLoginUserEntity(username);

        checkOwnerOrAdmin(community, loginUser, "삭제 권한이 없습니다.");

        deleteAllStoredImages(community);

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

    private List<MultipartFile> extractValidFiles(MultipartFile[] image) {
        List<MultipartFile> validFiles = new ArrayList<>();

        if (image == null) {
            return validFiles;
        }

        for (MultipartFile file : image) {
            if (file != null && !file.isEmpty()) {
                validFiles.add(file);
            }
        }

        return validFiles;
    }

    private void saveImages(Community community, MultipartFile[] image, int mainIndex) {
        List<MultipartFile> validFiles = extractValidFiles(image);

        if (validFiles.isEmpty()) {
            return;
        }

        if (validFiles.size() > MAX_FILE_COUNT) {
            throw new IllegalArgumentException("사진은 최대 " + MAX_FILE_COUNT + "개까지 첨부할 수 있어요.");
        }

        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("업로드 폴더 생성 중 오류가 발생했습니다.");
        }

        int normalizedMainIndex = (mainIndex >= 0 && mainIndex < validFiles.size()) ? mainIndex : 0;

        for (int i = 0; i < validFiles.size(); i++) {
            MultipartFile file = validFiles.get(i);

            if (file.getSize() > MAX_FILE_SIZE) {
                throw new IllegalArgumentException("사진 용량은 10MB를 초과할 수 없어요.");
            }

            String originalFileName = file.getOriginalFilename();
            if (originalFileName == null || originalFileName.isBlank()) {
                throw new IllegalArgumentException("잘못된 파일입니다.");
            }

            String extension = getExtension(originalFileName).toLowerCase();
            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                throw new IllegalArgumentException("이미지 파일(jpg, jpeg, png, gif, webp)만 업로드할 수 있어요.");
            }

            String storedFileName = UUID.randomUUID() + "." + extension;
            Path filePath = uploadPath.resolve(storedFileName);

            try {
                file.transferTo(filePath.toFile());
            } catch (Exception e) {
                throw new IllegalArgumentException("파일 저장 중 오류가 발생했습니다.");
            }

            CommunityImage communityImage = new CommunityImage();
            communityImage.setCommunity(community);
            communityImage.setOriginalFileName(originalFileName);
            communityImage.setStoredFileName(storedFileName);
            communityImage.setFileUrl("/uploads/community/" + storedFileName);
            communityImage.setSortOrder(i);
            communityImage.setMain(i == normalizedMainIndex);

            community.getImages().add(communityImage);
        }
    }

    private void removeImages(Community community, List<Long> deleteImageIds) {
        if (deleteImageIds == null || deleteImageIds.isEmpty()) {
            return;
        }

        List<CommunityImage> toRemove = community.getImages().stream()
                .filter(img -> deleteImageIds.contains(img.getId()))
                .toList();

        for (CommunityImage img : toRemove) {
            try {
                Path filePath = Paths.get(uploadDir)
                        .toAbsolutePath()
                        .normalize()
                        .resolve(img.getStoredFileName());
                Files.deleteIfExists(filePath);
            } catch (Exception e) {
                // 개별 파일 삭제 실패는 무시하고 계속 진행
            }
        }

        community.getImages().removeAll(toRemove);
    }

    private List<CommunityImage> appendImages(Community community, List<MultipartFile> validFiles) {
        List<CommunityImage> added = new ArrayList<>();

        if (validFiles.isEmpty()) {
            return added;
        }

        int existingCount = community.getImages().size();

        if (existingCount + validFiles.size() > MAX_FILE_COUNT) {
            throw new IllegalArgumentException("사진은 최대 " + MAX_FILE_COUNT + "개까지 첨부할 수 있어요.");
        }

        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("업로드 폴더 생성 중 오류가 발생했습니다.");
        }

        int nextSortOrder = community.getImages().stream()
                .mapToInt(CommunityImage::getSortOrder)
                .max()
                .orElse(-1) + 1;

        for (int i = 0; i < validFiles.size(); i++) {
            MultipartFile file = validFiles.get(i);

            if (file.getSize() > MAX_FILE_SIZE) {
                throw new IllegalArgumentException("사진 용량은 10MB를 초과할 수 없어요.");
            }

            String originalFileName = file.getOriginalFilename();
            if (originalFileName == null || originalFileName.isBlank()) {
                throw new IllegalArgumentException("잘못된 파일입니다.");
            }

            String extension = getExtension(originalFileName).toLowerCase();
            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                throw new IllegalArgumentException("이미지 파일(jpg, jpeg, png, gif, webp)만 업로드할 수 있어요.");
            }

            String storedFileName = UUID.randomUUID() + "." + extension;
            Path filePath = uploadPath.resolve(storedFileName);

            try {
                file.transferTo(filePath.toFile());
            } catch (Exception e) {
                throw new IllegalArgumentException("파일 저장 중 오류가 발생했습니다.");
            }

            CommunityImage communityImage = new CommunityImage();
            communityImage.setCommunity(community);
            communityImage.setOriginalFileName(originalFileName);
            communityImage.setStoredFileName(storedFileName);
            communityImage.setFileUrl("/uploads/community/" + storedFileName);
            communityImage.setSortOrder(nextSortOrder + i);
            communityImage.setMain(false);

            community.getImages().add(communityImage);
            added.add(communityImage);
        }

        return added;
    }

    private void applyMainSelection(Community community, String mainSelect, List<CommunityImage> newImages) {
        for (CommunityImage img : community.getImages()) {
            img.setMain(false);
        }

        if (community.getImages().isEmpty()) {
            return;
        }

        if (mainSelect != null && mainSelect.startsWith("existing-")) {
            Long selectedId = Long.valueOf(mainSelect.substring("existing-".length()));

            boolean matched = community.getImages().stream()
                    .filter(img -> img.getId() != null && img.getId().equals(selectedId))
                    .findFirst()
                    .map(img -> {
                        img.setMain(true);
                        return true;
                    })
                    .orElse(false);

            if (matched) {
                return;
            }
        }

        if (mainSelect != null && mainSelect.startsWith("new-")) {
            int idx = Integer.parseInt(mainSelect.substring("new-".length()));

            if (idx >= 0 && idx < newImages.size()) {
                newImages.get(idx).setMain(true);
                return;
            }
        }

        community.getImages().get(0).setMain(true);
    }

    private void deleteAllStoredImages(Community community) {
        if (community.getImages() == null) {
            return;
        }

        for (CommunityImage image : community.getImages()) {
            try {
                Path filePath = Paths.get(uploadDir)
                        .toAbsolutePath()
                        .normalize()
                        .resolve(image.getStoredFileName());

                Files.deleteIfExists(filePath);
            } catch (Exception e) {
                // 개별 파일 삭제 실패는 무시하고 계속 진행
            }
        }
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