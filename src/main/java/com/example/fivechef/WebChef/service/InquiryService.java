package com.example.fivechef.WebChef.service;

import com.example.fivechef.WebChef.dto.InquiryCreateRequest;
import com.example.fivechef.WebChef.dto.InquiryResponse;
import com.example.fivechef.WebChef.dto.InquiryUpdateRequest;
import com.example.fivechef.WebChef.entity.Inquiry;
import com.example.fivechef.WebChef.entity.Role;
import com.example.fivechef.WebChef.entity.User;
import com.example.fivechef.WebChef.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public Inquiry getInquiryEntity(Long id) {
        return inquiryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("문의사항을 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public Page<InquiryResponse> getInquiries(int page, String keyword) {
        Pageable pageable = PageRequest.of(
                page,
                10,
                Sort.by(Sort.Order.desc("id"))
        );

        if (isBlank(keyword)) {
            return inquiryRepository.findAll(pageable)
                    .map(InquiryResponse::new);
        }

        String kw = keyword.trim();

        return inquiryRepository.findBySubjectContainingOrContentContaining(
                        kw,
                        kw,
                        pageable
                )
                .map(InquiryResponse::new);
    }

    @Transactional(readOnly = true)
    public InquiryResponse getInquiryResponse(Long id) {
        return new InquiryResponse(getInquiryEntity(id));
    }

    @Transactional
    public void createInquiry(InquiryCreateRequest request, String username) {
        validateCreateRequest(request);

        User author = userService.getLoginUserEntity(username);

        Inquiry inquiry = new Inquiry();
        inquiry.setSubject(request.getSubject().trim());
        inquiry.setContent(request.getContent().trim());
        inquiry.setAuthor(author);
        inquiry.setAnswered(false);

        inquiryRepository.save(inquiry);
    }

    @Transactional
    public void updateInquiry(Long id, InquiryUpdateRequest request, String username) {
        validateUpdateRequest(request);

        Inquiry inquiry = getInquiryEntity(id);
        User loginUser = userService.getLoginUserEntity(username);

        checkOwnerOrAdmin(inquiry, loginUser, "수정 권한이 없습니다.");

        inquiry.setSubject(request.getSubject().trim());
        inquiry.setContent(request.getContent().trim());

        inquiryRepository.save(inquiry);
    }

    @Transactional
    public void answerInquiry(Long id, String answerContent) {
        if (isBlank(answerContent)) {
            throw new IllegalArgumentException("답변 내용을 입력해주세요.");
        }

        Inquiry inquiry = getInquiryEntity(id);
        inquiry.setAnswerContent(answerContent.trim());
        inquiry.setAnswered(true);
        inquiry.setAnswerDate(LocalDateTime.now());

        inquiryRepository.save(inquiry);
    }

    @Transactional
    public void deleteInquiry(Long id, String username) {
        Inquiry inquiry = getInquiryEntity(id);
        User loginUser = userService.getLoginUserEntity(username);

        checkOwnerOrAdmin(inquiry, loginUser, "삭제 권한이 없습니다.");

        inquiryRepository.delete(inquiry);
    }

    private void validateCreateRequest(InquiryCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("문의 등록 정보가 없습니다.");
        }

        if (isBlank(request.getSubject())) {
            throw new IllegalArgumentException("제목을 입력해주세요.");
        }

        if (isBlank(request.getContent())) {
            throw new IllegalArgumentException("내용을 입력해주세요.");
        }
    }

    private void validateUpdateRequest(InquiryUpdateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("문의 수정 정보가 없습니다.");
        }

        if (isBlank(request.getSubject())) {
            throw new IllegalArgumentException("제목을 입력해주세요.");
        }

        if (isBlank(request.getContent())) {
            throw new IllegalArgumentException("내용을 입력해주세요.");
        }
    }

    private void checkOwnerOrAdmin(Inquiry inquiry, User loginUser, String message) {
        boolean isAdmin = loginUser.getRole() == Role.ADMIN;
        boolean isOwner = inquiry.getAuthor() != null
                && inquiry.getAuthor().getId().equals(loginUser.getId());

        if (!isAdmin && !isOwner) {
            throw new IllegalArgumentException(message);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}