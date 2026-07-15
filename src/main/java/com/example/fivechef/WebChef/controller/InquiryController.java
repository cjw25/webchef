package com.example.fivechef.WebChef.controller;

import com.example.fivechef.WebChef.dto.InquiryAnswerRequest;
import com.example.fivechef.WebChef.dto.InquiryCreateRequest;
import com.example.fivechef.WebChef.dto.InquiryResponse;
import com.example.fivechef.WebChef.dto.InquiryUpdateRequest;
import com.example.fivechef.WebChef.service.InquiryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@RequiredArgsConstructor
@Controller
@RequestMapping("/inquiry")
public class InquiryController {

    private final InquiryService inquiryService;

    /**
     * 문의 목록
     */
    @GetMapping("/list")
    public String list(
            Model model,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "kw", required = false) String kw
    ) {
        Page<InquiryResponse> paging =
                inquiryService.getInquiries(page, kw);

        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);

        return "inquiry/list";
    }

    /**
     * 문의 상세
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/view/{id}")
    public String view(
            @PathVariable("id") Long id,
            Model model,
            Principal principal
    ) {
        InquiryResponse inquiry =
                inquiryService.getInquiryResponse(
                        id,
                        principal.getName()
                );

        InquiryAnswerRequest answerRequest =
                new InquiryAnswerRequest();

        answerRequest.setInquiryId(id);

        model.addAttribute("inquiry", inquiry);
        model.addAttribute("answerRequest", answerRequest);

        return "inquiry/view";
    }

    /**
     * 문의 등록 화면
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/create")
    public String createPage(Model model) {

        model.addAttribute(
                "request",
                new InquiryCreateRequest()
        );

        return "inquiry/create";
    }

    /**
     * 문의 등록
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create")
    public String createInquiry(
            @ModelAttribute("request")
            InquiryCreateRequest request,

            @RequestParam(
                    value = "img",
                    required = false
            )
            MultipartFile[] img,

            Model model,
            Principal principal
    ) {
        try {
            inquiryService.createInquiry(
                    request,
                    principal.getName(),
                    img
            );
        } catch (Exception e) {
            model.addAttribute(
                    "errorMessage",
                    e.getMessage()
            );

            return "inquiry/create";
        }

        return "redirect:/inquiry/list";
    }

    /**
     * 문의 수정 화면
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/update/{id}")
    public String updatePage(
            @PathVariable("id") Long id,
            Model model,
            Principal principal
    ) {
        InquiryResponse inquiry =
                inquiryService.getInquiryResponse(
                        id,
                        principal.getName()
                );

        if (!inquiry.isMine()) {
            throw new IllegalArgumentException(
                    "수정 권한이 없습니다."
            );
        }

        InquiryUpdateRequest request =
                new InquiryUpdateRequest();

        request.setSubject(inquiry.getSubject());
        request.setContent(inquiry.getContent());

        model.addAttribute("inquiry", inquiry);
        model.addAttribute("request", request);

        return "inquiry/update";
    }

    /**
     * 문의 수정
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/update/{id}")
    public String updateInquiry(
            @PathVariable("id") Long id,

            @ModelAttribute("request")
            InquiryUpdateRequest request,

            @RequestParam(
                    value = "img",
                    required = false
            )
            MultipartFile[] img,

            Model model,
            Principal principal
    ) {
        try {
            inquiryService.updateInquiry(
                    id,
                    request,
                    principal.getName(),
                    img
            );
        } catch (Exception e) {
            InquiryResponse inquiry =
                    inquiryService.getInquiryResponse(
                            id,
                            principal.getName()
                    );

            model.addAttribute("inquiry", inquiry);
            model.addAttribute("request", request);
            model.addAttribute(
                    "errorMessage",
                    e.getMessage()
            );

            return "inquiry/update";
        }

        return "redirect:/inquiry/view/" + id;
    }

    /**
     * 문의 삭제
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/delete/{id}")
    public String deleteInquiry(
            @PathVariable("id") Long id,
            Principal principal
    ) {
        inquiryService.deleteInquiry(
                id,
                principal.getName()
        );

        return "redirect:/inquiry/list";
    }
}