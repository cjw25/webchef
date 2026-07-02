package com.example.fivechef.WebChef.service;

import com.example.fivechef.WebChef.dto.AnswerCreateRequest;
import com.example.fivechef.WebChef.dto.AnswerResponse;
import com.example.fivechef.WebChef.dto.AnswerUpdateRequest;
import com.example.fivechef.WebChef.entity.Answer;
import com.example.fivechef.WebChef.entity.Community;
import com.example.fivechef.WebChef.entity.Role;
import com.example.fivechef.WebChef.entity.User;
import com.example.fivechef.WebChef.repository.AnswerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final CommunityService communityService;
    private final UserService userService;

    @Transactional(readOnly = true)
    public Answer getAnswerEntity(Long id) {
        return answerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public AnswerResponse getAnswerResponse(Long id) {
        return new AnswerResponse(getAnswerEntity(id));
    }

    @Transactional
    public AnswerResponse createAnswer(AnswerCreateRequest request, String username) {
        validateCreateRequest(request);

        Community community = communityService.getCommunityEntity(request.getCommunityId());
        User author = userService.getLoginUserEntity(username);

        Answer answer = new Answer();
        answer.setContent(request.getContent().trim());
        answer.setCommunity(community);
        answer.setAuthor(author);

        Answer savedAnswer = answerRepository.save(answer);

        return new AnswerResponse(savedAnswer);
    }

    @Transactional
    public void updateAnswer(Long id, AnswerUpdateRequest request, String username) {
        validateUpdateRequest(request);

        Answer answer = getAnswerEntity(id);
        User loginUser = userService.getLoginUserEntity(username);

        checkOwnerOrAdmin(answer, loginUser, "수정 권한이 없습니다.");

        answer.setContent(request.getContent().trim());

        answerRepository.save(answer);
    }

    @Transactional
    public Long deleteAnswer(Long id, String username) {
        Answer answer = getAnswerEntity(id);
        User loginUser = userService.getLoginUserEntity(username);

        checkOwnerOrAdmin(answer, loginUser, "삭제 권한이 없습니다.");

        Long communityId = answer.getCommunity().getId();

        answerRepository.delete(answer);

        return communityId;
    }

    @Transactional
    public Long voteAnswer(Long id, String username) {
        Answer answer = getAnswerEntity(id);
        User loginUser = userService.getLoginUserEntity(username);

        boolean alreadyVoted = answer.getVoter()
                .removeIf(user -> user.getId().equals(loginUser.getId()));

        if (!alreadyVoted) {
            answer.getVoter().add(loginUser);
        }

        answerRepository.save(answer);

        return answer.getCommunity().getId();
    }

    private void validateCreateRequest(AnswerCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("댓글 등록 정보가 없습니다.");
        }

        if (request.getCommunityId() == null) {
            throw new IllegalArgumentException("게시글 정보가 없습니다.");
        }

        if (isBlank(request.getContent())) {
            throw new IllegalArgumentException("댓글 내용을 입력해주세요.");
        }
    }

    private void validateUpdateRequest(AnswerUpdateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("댓글 수정 정보가 없습니다.");
        }

        if (isBlank(request.getContent())) {
            throw new IllegalArgumentException("댓글 내용을 입력해주세요.");
        }
    }

    private void checkOwnerOrAdmin(Answer answer, User loginUser, String message) {
        boolean isAdmin = loginUser.getRole() == Role.ADMIN;
        boolean isOwner = answer.getAuthor() != null
                && answer.getAuthor().getId().equals(loginUser.getId());

        if (!isAdmin && !isOwner) {
            throw new IllegalArgumentException(message);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}