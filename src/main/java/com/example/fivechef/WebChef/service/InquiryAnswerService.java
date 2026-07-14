package com.example.fivechef.WebChef.service;

import com.example.fivechef.WebChef.dto.InquiryAnswerRequest;
import com.example.fivechef.WebChef.dto.InquiryAnswerResponse;
import com.example.fivechef.WebChef.entity.Inquiry;
import com.example.fivechef.WebChef.entity.InquiryAnswer;
import com.example.fivechef.WebChef.entity.Role;
import com.example.fivechef.WebChef.entity.User;
import com.example.fivechef.WebChef.repository.InquiryAnswerRepository;
import com.example.fivechef.WebChef.repository.InquiryRepository;
import com.example.fivechef.WebChef.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryAnswerService {

    private final InquiryAnswerRepository repository;
    private final InquiryRepository inquiryRepository;
    private final UserRepository userRepository;

    public List<InquiryAnswerResponse> getAnswerList(Long inquiryId) {
        return repository.findByInquiryIdOrderByCreateDateAsc(inquiryId)
                .stream()
                .map(InquiryAnswerResponse::new)
                .toList();
    }

    @Transactional
    public Long createAnswer(
            InquiryAnswerRequest request,
            String username
    ) {
        if (request == null) {
            throw new IllegalArgumentException("답변 정보가 없습니다.");
        }

        if (request.getInquiryId() == null) {
            throw new IllegalArgumentException("문의 번호가 없습니다.");
        }

        if (request.getContent() == null
                || request.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("답변 내용을 입력해주세요.");
        }

        if (request.getContent().trim().length() > 1000) {
            throw new IllegalArgumentException(
                    "답변은 1000자 이내로 입력해주세요."
            );
        }

        User loginUser = getUser(username);
        Inquiry inquiry = getInquiry(request.getInquiryId());

        validateAnswerPermission(inquiry, loginUser);

        InquiryAnswer answer = new InquiryAnswer();
        answer.setInquiry(inquiry);
        answer.setAuthor(loginUser);
        answer.setContent(request.getContent().trim());

        InquiryAnswer savedAnswer = repository.save(answer);

        return savedAnswer.getId();
    }

    /**
     * 문의 답변 수정
     */
    @Transactional
    public void updateAnswer(
            Long answerId,
            String content,
            String username
    ) {
        InquiryAnswer answer = getAnswer(answerId);
        User loginUser = getUser(username);

        validateModifyPermission(answer, loginUser);

        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "답변 내용을 입력해주세요."
            );
        }

        String trimmedContent = content.trim();

        if (trimmedContent.length() > 1000) {
            throw new IllegalArgumentException(
                    "답변은 1000자 이내로 입력해주세요."
            );
        }

        answer.setContent(trimmedContent);
    }

    /**
     * 문의 답변 삭제
     */
    @Transactional
    public void deleteAnswer(
            Long answerId,
            String username
    ) {
        InquiryAnswer answer = getAnswer(answerId);
        User loginUser = getUser(username);

        validateModifyPermission(answer, loginUser);

        repository.delete(answer);
    }

    /**
     * 답변 작성 권한 확인
     *
     * 문의 작성자 또는 관리자만 답변 가능
     */
    private void validateAnswerPermission(
            Inquiry inquiry,
            User loginUser
    ) {
        boolean admin =
                loginUser.getRole() == Role.ADMIN;

        boolean inquiryOwner =
                inquiry.getAuthor() != null
                        && inquiry.getAuthor().getId()
                        .equals(loginUser.getId());

        if (!admin && !inquiryOwner) {
            throw new AccessDeniedException(
                    "문의 작성자와 관리자만 답변을 작성할 수 있습니다."
            );
        }
    }

    /**
     * 답변 수정 및 삭제 권한 확인
     *
     * 답변 작성자 또는 관리자만 가능
     */
    private void validateModifyPermission(
            InquiryAnswer answer,
            User loginUser
    ) {
        boolean admin =
                loginUser.getRole() == Role.ADMIN;

        boolean answerOwner =
                answer.getAuthor() != null
                        && answer.getAuthor().getId()
                        .equals(loginUser.getId());

        if (!admin && !answerOwner) {
            throw new AccessDeniedException(
                    "답변을 수정하거나 삭제할 권한이 없습니다."
            );
        }
    }

    private InquiryAnswer getAnswer(Long answerId) {

        return repository
                .findById(answerId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "답변을 찾을 수 없습니다."
                        )
                );
    }

    private Inquiry getInquiry(Long inquiryId) {

        return inquiryRepository
                .findById(inquiryId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "문의를 찾을 수 없습니다."
                        )
                );
    }

    private User getUser(String username) {

        return userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "사용자를 찾을 수 없습니다."
                        )
                );
    }


}
