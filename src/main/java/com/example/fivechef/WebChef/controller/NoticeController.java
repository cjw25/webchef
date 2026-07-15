package com.example.fivechef.WebChef.controller;

import com.example.fivechef.WebChef.dto.NoticeCreateRequest;
import com.example.fivechef.WebChef.dto.NoticeResponse;
import com.example.fivechef.WebChef.dto.NoticeUpdateRequest;
import com.example.fivechef.WebChef.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

@RequiredArgsConstructor
@Controller
public class NoticeController {

    private final NoticeService noticeService;

    /**
     * 공지사항 목록
     */
    @GetMapping("/notice/list")
    public String list(
            Model model,
            @RequestParam(
                    value = "page",
                    defaultValue = "0"
            )
            int page,
            @RequestParam(
                    value = "kw",
                    required = false
            )
            String kw
    ) {
        /*
         * 음수 페이지 요청 방지
         */
        if (page < 0) {
            page = 0;
        }

        Page<NoticeResponse> paging =
                noticeService.getNotices(
                        page,
                        kw
                );

        model.addAttribute(
                "paging",
                paging
        );

        model.addAttribute(
                "kw",
                kw
        );

        return "notice/list";
    }

    /**
     * 공지사항 상세
     */
    @GetMapping("/notice/view/{id}")
    public String view(
            @PathVariable("id")
            Long id,
            Model model
    ) {
        NoticeResponse notice =
                noticeService.getNoticeDetail(id);

        model.addAttribute(
                "notice",
                notice
        );

        return "notice/view";
    }

    /**
     * 공지사항 등록 화면
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/notice/create")
    public String createPage(
            Model model
    ) {
        model.addAttribute(
                "request",
                new NoticeCreateRequest()
        );

        return "notice/create";
    }

    /**
     * 공지사항 등록 처리
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/notice/create")
    public String createNotice(
            @ModelAttribute("request")
            NoticeCreateRequest request,

            @RequestParam(
                    value = "images",
                    required = false
            )
            List<MultipartFile> images,

            Model model,
            Principal principal
    ) {
        try {
            if (principal == null) {
                throw new IllegalStateException(
                        "로그인 정보가 없습니다."
                );
            }

            noticeService.createNotice(
                    request,
                    principal.getName(),
                    images
            );

        } catch (Exception e) {
            model.addAttribute(
                    "errorMessage",
                    e.getMessage()
            );

            return "notice/create";
        }

        return "redirect:/notice/list";
    }

    /**
     * 공지사항 수정 화면
     *
     * 조회수가 증가하지 않는 조회 메서드를 사용한다.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/notice/update/{id}")
    public String updatePage(
            @PathVariable("id")
            Long id,
            Model model
    ) {
        NoticeResponse notice =
                noticeService.getNoticeResponse(id);

        NoticeUpdateRequest request =
                new NoticeUpdateRequest();

        request.setSubject(
                notice.getSubject()
        );

        request.setContent(
                notice.getContent()
        );

        model.addAttribute(
                "notice",
                notice
        );

        model.addAttribute(
                "request",
                request
        );

        return "notice/update";
    }

    /**
     * 공지사항 수정 처리
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/notice/update/{id}")
    public String updateNotice(
            @PathVariable("id")
            Long id,

            @ModelAttribute("request")
            NoticeUpdateRequest request,

            @RequestParam(
                    value = "images",
                    required = false
            )
            List<MultipartFile> newImages,

            @RequestParam(
                    value = "deleteImageIds",
                    required = false
            )
            List<Long> deleteImageIds,

            Model model
    ) {
        try {
            noticeService.updateNotice(
                    id,
                    request,
                    newImages,
                    deleteImageIds
            );

        } catch (Exception e) {
            NoticeResponse notice =
                    noticeService
                            .getNoticeResponse(id);

            model.addAttribute(
                    "notice",
                    notice
            );

            model.addAttribute(
                    "errorMessage",
                    e.getMessage()
            );

            return "notice/update";
        }

        return "redirect:/notice/view/" + id;
    }

    /**
     * 공지사항 삭제 처리
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/notice/delete/{id}")
    public String deleteNotice(
            @PathVariable("id")
            Long id
    ) {
        noticeService.deleteNotice(id);

        return "redirect:/notice/list";
    }
}
