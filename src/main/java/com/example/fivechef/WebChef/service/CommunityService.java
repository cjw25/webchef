package com.example.fivechef.WebChef.service;

import com.example.fivechef.WebChef.dto.CommunityCreateRequest;
import com.example.fivechef.WebChef.dto.CommunityResponse;
import com.example.fivechef.WebChef.dto.CommunityUpdateRequest;
import com.example.fivechef.WebChef.entity.Community;
import com.example.fivechef.WebChef.entity.Role;
import com.example.fivechef.WebChef.entity.User;
import com.example.fivechef.WebChef.repository.CommunityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CommunityService {

    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png", "gif", "webp");
    private static final long MAX_FILE_SIZE = 10 * 2024 * 2024;
    private static final int MAX_FILE_COUNT = 5;

    private final CommunityRepository communityRepository;
    private final UserService userService;

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
    public void createCommunity(CommunityCreateRequest request, String username,
                                MultipartFile[] image) {
        validateCreateRequest(request);

        User author = userService.getLoginUserEntity(username);

        Community community = new Community();
        community.setCategory(request.getCategory());
        community.setSubject(request.getSubject().trim());
        community.setContent(request.getContent().trim());
        community.setAuthor(author);

        communityRepository.save(community);

        if(image != null){
            if (image.length > MAX_FILE_COUNT) {
                throw new IllegalArgumentException("사진은 최대 " + MAX_FILE_COUNT +
                        "개까지 첨부할 수 있어요.");
            }

            java.io.File uploadFolder = new java.io.File(uploadDir);
            if (!uploadFolder.exists()){
                    uploadFolder.mkdirs();
            }

            for (MultipartFile file : image) {
                if (file.isEmpty()) continue;

                if (file.getSize() > MAX_FILE_SIZE) {
                    throw new IllegalArgumentException("사진 용량은 10MB 초과할 수 없어요.");
                }

                String originalName = file.getOriginalFilename();
                if (originalName == null || originalName.isBlank()) {
                    throw new IllegalArgumentException("잘못된 파일입니다.");
                }

                String ext = getExtension(originalName).toLowerCase();
                if (!ALLOWED_EXTENSIONS.contains(ext)) {
                    throw new IllegalArgumentException("이미지 파일(jpg, png, gif, webp)만 업로드 할 수 있어요.");
                }

                String saveName = java.util.UUID.randomUUID() + "." + ext;
                String path = uploadDir + saveName;

                try {
                    file.transferTo(new java.io.File(path));
                } catch (Exception e) {
                    throw new RuntimeException("파일 저장에 실패했습니다.", e);
                }

            }
        }
    }

    private String getExtension(String filename) {
        int dotIdx = filename.lastIndexOf(".");
        if (dotIdx == -1 || dotIdx == filename.length() - 1) {
            throw new IllegalArgumentException("확장자가 없는 파일은 업로드할 수 없어요.");
        }
        return filename.substring(dotIdx + 1);
    }

    @Transactional
    public void updateCommunity(Long id, CommunityUpdateRequest request, String username) {
        validateUpdateRequest(request);

        Community community = getCommunityEntity(id);
        User loginUser = userService.getLoginUserEntity(username);

        checkOwnerOrAdmin(community, loginUser, "수정 권한이 없습니다.");

        community.setCategory(request.getCategory());
        community.setSubject(request.getSubject().trim());
        community.setContent(request.getContent().trim());

        communityRepository.save(community);
    }

    @Transactional
    public void deleteCommunity(Long id, String username) {
        Community community = getCommunityEntity(id);
        User loginUser = userService.getLoginUserEntity(username);

        checkOwnerOrAdmin(community, loginUser, "삭제 권한이 없습니다.");

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