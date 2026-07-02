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

@RequiredArgsConstructor
@Service
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final UserService userService;

    // =========================
    // Entity 조회 - Service 내부용
    // =========================

    @Transactional(readOnly = true)
    public Community getCommunityEntity(Long id) {
        return communityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
    }

    // 기존 코드 호환용
    @Transactional(readOnly = true)
    public Community view(Long id) {
        return getCommunityEntity(id);
    }

    // =========================
    // Response 반환 - Controller 전달용
    // =========================

    @Transactional(readOnly = true)
    public Page<CommunityResponse> getCommunities(int page, String keyword) {
        Pageable pageable = PageRequest.of(
                page,
                10,
                Sort.by(Sort.Order.desc("id"))
        );

        if (keyword == null || keyword.trim().isEmpty()) {
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

    // 기존 코드 호환용
    @Transactional(readOnly = true)
    public Page<CommunityResponse> list(int page, String keyword) {
        return getCommunities(page, keyword);
    }

    @Transactional(readOnly = true)
    public CommunityResponse getCommunityResponse(Long id) {
        Community community = getCommunityEntity(id);
        return new CommunityResponse(community, true);
    }

    // =========================
    // 게시글 등록
    // =========================

    @Transactional
    public void createCommunity(CommunityCreateRequest request, String username) {
        validateCreateRequest(request);

        User author = userService.getLoginUserEntity(username);

        Community community = new Community();
        community.setSubject(request.getSubject().trim());
        community.setContent(request.getContent().trim());
        community.setAuthor(author);

        communityRepository.save(community);
    }

    // =========================
    // 게시글 수정
    // =========================

    @Transactional
    public void updateCommunity(Long id, CommunityUpdateRequest request, String username) {
        validateUpdateRequest(request);

        Community community = getCommunityEntity(id);
        User loginUser = userService.getLoginUserEntity(username);

        checkOwnerOrAdmin(community, loginUser, "수정권한이 없습니다.");

        community.setSubject(request.getSubject().trim());
        community.setContent(request.getContent().trim());

        communityRepository.save(community);
    }

    // =========================
    // 게시글 삭제
    // =========================

    @Transactional
    public void deleteCommunity(Long id, String username) {
        Community community = getCommunityEntity(id);
        User loginUser = userService.getLoginUserEntity(username);

        checkOwnerOrAdmin(community, loginUser, "삭제권한이 없습니다.");

        communityRepository.delete(community);
    }

    // =========================
    // 게시글 추천 토글
    // =========================

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

    // =========================
    // 검증
    // =========================

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