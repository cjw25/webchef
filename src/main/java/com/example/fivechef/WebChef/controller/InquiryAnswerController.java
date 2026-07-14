package com.example.fivechef.WebChef.controller;

import com.example.fivechef.WebChef.dto.InquiryAnswerRequest;
import com.example.fivechef.WebChef.dto.InquiryResponse;
import com.example.fivechef.WebChef.service.InquiryAnswerService;
import com.example.fivechef.WebChef.service.InquiryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/inquiry-answer")
public class InquiryAnswerController {

    private final InquiryAnswerService inquiryAnswerService;
    private final InquiryService inquiryService;

    /**
     * 문의 답변 등록
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/create")
    public String createAnswer(
            @Valid @ModelAttribute("answerRequest")
            InquiryAnswerRequest request,
            BindingResult bindingResult,
            Model model,
            Principal principal
    ) {
        Long inquiryId = request.getInquiryId();

        if (inquiryId == null) {
            throw new IllegalArgumentException("문의 번호가 없습니다.");
        }

        if (bindingResult.hasErrors()) {
            InquiryResponse inquiry =
                    inquiryService.getInquiryResponse(
                            inquiryId,
                            principal.getName()
                    );

            model.addAttribute("inquiry", inquiry);

            return "inquiry/view";
        }

        try {
            inquiryAnswerService.createAnswer(
                    request,
                    principal.getName()
            );
        } catch (Exception e) {
            InquiryResponse inquiry =
                    inquiryService.getInquiryResponse(
                            inquiryId,
                            principal.getName()
                    );

            model.addAttribute("inquiry", inquiry);
            model.addAttribute(
                    "errorMessage",
                    e.getMessage()
            );

            return "inquiry/view";
        }

        return "redirect:/inquiry/view/" + inquiryId;
    }

    /**
     * 문의 답변 수정
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/update/{answerId}")
    public String updateAnswer(
            @PathVariable("answerId") Long answerId,
            @RequestParam("inquiryId") Long inquiryId,
            @RequestParam("content") String content,
            Principal principal
    ) {
        inquiryAnswerService.updateAnswer(
                answerId,
                content,
                principal.getName()
        );

        return "redirect:/inquiry/view/" + inquiryId;
    }

    /**
     * 문의 답변 삭제
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/delete/{answerId}")
    public String deleteAnswer(
            @PathVariable("answerId") Long answerId,
            @RequestParam("inquiryId") Long inquiryId,
            Principal principal
    ) {
        inquiryAnswerService.deleteAnswer(
                answerId,
                principal.getName()
        );

        return "redirect:/inquiry/view/" + inquiryId;
    }
}