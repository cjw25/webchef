package com.example.fivechef.WebChef.service;

import com.example.fivechef.WebChef.dto.NoticeCreateRequest;
import com.example.fivechef.WebChef.dto.NoticeResponse;
import com.example.fivechef.WebChef.dto.NoticeUpdateRequest;
import com.example.fivechef.WebChef.entity.Notice;
import com.example.fivechef.WebChef.entity.User;
import com.example.fivechef.WebChef.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public Notice getNoticeEntity(Long id) {
        return noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("공지사항을 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public Page<NoticeResponse> getNotices(int page, String keyword) {
        Pageable pageable = PageRequest.of(
                page,
                10,
                Sort.by(Sort.Order.desc("id"))
        );

        if (isBlank(keyword)) {
            return noticeRepository.findAll(pageable)
                    .map(NoticeResponse::new);
        }

        String kw = keyword.trim();

        return noticeRepository.findBySubjectContainingOrContentContaining(
                        kw,
                        kw,
                        pageable
                )
                .map(NoticeResponse::new);
    }

    @Transactional(readOnly = true)
    public NoticeResponse getNoticeResponse(Long id) {
        return new NoticeResponse(getNoticeEntity(id));
    }

    @Transactional
    public void createNotice(NoticeCreateRequest request, String username) {
        validateCreateRequest(request);

        User author = userService.getLoginUserEntity(username);

        Notice notice = new Notice();
        notice.setSubject(request.getSubject().trim());
        notice.setContent(request.getContent().trim());
        notice.setAuthor(author);

        noticeRepository.save(notice);
    }

    @Transactional
    public void updateNotice(Long id, NoticeUpdateRequest request) {
        validateUpdateRequest(request);

        Notice notice = getNoticeEntity(id);
        notice.setSubject(request.getSubject().trim());
        notice.setContent(request.getContent().trim());

        noticeRepository.save(notice);
    }

    @Transactional
    public void deleteNotice(Long id) {
        Notice notice = getNoticeEntity(id);
        noticeRepository.delete(notice);
    }

    private void validateCreateRequest(NoticeCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("공지사항 등록 정보가 없습니다.");
        }

        if (isBlank(request.getSubject())) {
            throw new IllegalArgumentException("제목을 입력해주세요.");
        }

        if (isBlank(request.getContent())) {
            throw new IllegalArgumentException("내용을 입력해주세요.");
        }
    }

    private void validateUpdateRequest(NoticeUpdateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("공지사항 수정 정보가 없습니다.");
        }

        if (isBlank(request.getSubject())) {
            throw new IllegalArgumentException("제목을 입력해주세요.");
        }

        if (isBlank(request.getContent())) {
            throw new IllegalArgumentException("내용을 입력해주세요.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}