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
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RequiredArgsConstructor
@Controller
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping("/notice/list")
    public String list(
            Model model,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "kw", required = false) String kw
    ) {
        Page<NoticeResponse> paging = noticeService.getNotices(page, kw);

        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);

        return "notice/list";
    }

    @GetMapping("/notice/view/{id}")
    public String view(
            @PathVariable("id") Long id,
            Model model
    ) {
        NoticeResponse notice = noticeService.getNoticeResponse(id);

        model.addAttribute("notice", notice);

        return "notice/view";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/notice/create")
    public String createPage(Model model) {
        model.addAttribute("request", new NoticeCreateRequest());
        return "notice/create";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/notice/create")
    public String createNotice(
            @ModelAttribute("request") NoticeCreateRequest request,
            Model model,
            Principal principal
    ) {
        try {
            noticeService.createNotice(request, principal.getName());
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "notice/create";
        }

        return "redirect:/notice/list";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/notice/update/{id}")
    public String updatePage(
            @PathVariable("id") Long id,
            Model model
    ) {
        NoticeResponse notice = noticeService.getNoticeResponse(id);

        NoticeUpdateRequest request = new NoticeUpdateRequest();
        request.setSubject(notice.getSubject());
        request.setContent(notice.getContent());

        model.addAttribute("notice", notice);
        model.addAttribute("request", request);

        return "notice/update";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/notice/update/{id}")
    public String updateNotice(
            @PathVariable("id") Long id,
            @ModelAttribute("request") NoticeUpdateRequest request,
            Model model
    ) {
        try {
            noticeService.updateNotice(id, request);
        } catch (Exception e) {
            NoticeResponse notice = noticeService.getNoticeResponse(id);

            model.addAttribute("notice", notice);
            model.addAttribute("errorMessage", e.getMessage());

            return "notice/update";
        }

        return "redirect:/notice/view/" + id;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/notice/delete/{id}")
    public String deleteNotice(@PathVariable("id") Long id) {
        noticeService.deleteNotice(id);
        return "redirect:/notice/list";
    }
}