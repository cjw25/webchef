package com.example.fivechef.WebChef.controller;

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

import java.security.Principal;

@RequiredArgsConstructor
@Controller
public class InquiryController {

    private final InquiryService inquiryService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/inquiry/list")
    public String list(
            Model model,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "kw", required = false) String kw
    ) {
        Page<InquiryResponse> paging = inquiryService.getInquiries(page, kw);

        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);

        return "inquiry/list";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/inquiry/view/{id}")
    public String view(
            @PathVariable("id") Long id,
            Model model
    ) {
        InquiryResponse inquiry = inquiryService.getInquiryResponse(id);

        InquiryUpdateRequest request = new InquiryUpdateRequest();
        request.setSubject(inquiry.getSubject());
        request.setContent(inquiry.getContent());
        request.setAnswerContent(inquiry.getAnswerContent());

        model.addAttribute("inquiry", inquiry);
        model.addAttribute("request", request);

        return "inquiry/view";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/inquiry/create")
    public String createPage(Model model) {
        model.addAttribute("request", new InquiryCreateRequest());
        return "inquiry/create";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/inquiry/create")
    public String createInquiry(
            @ModelAttribute("request") InquiryCreateRequest request,
            Model model,
            Principal principal
    ) {
        try {
            inquiryService.createInquiry(request, principal.getName());
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "inquiry/create";
        }

        return "redirect:/inquiry/list";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/inquiry/update/{id}")
    public String updateInquiry(
            @PathVariable("id") Long id,
            @ModelAttribute("request") InquiryUpdateRequest request,
            Model model,
            Principal principal
    ) {
        try {
            inquiryService.updateInquiry(id, request, principal.getName());
        } catch (Exception e) {
            InquiryResponse inquiry = inquiryService.getInquiryResponse(id);

            model.addAttribute("inquiry", inquiry);
            model.addAttribute("errorMessage", e.getMessage());

            return "inquiry/view";
        }

        return "redirect:/inquiry/view/" + id;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/inquiry/answer/{id}")
    public String answerInquiry(
            @PathVariable("id") Long id,
            @RequestParam("answerContent") String answerContent
    ) {
        inquiryService.answerInquiry(id, answerContent);

        return "redirect:/inquiry/view/" + id;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/inquiry/delete/{id}")
    public String deleteInquiry(
            @PathVariable("id") Long id,
            Principal principal
    ) {
        inquiryService.deleteInquiry(id, principal.getName());

        return "redirect:/inquiry/list";
    }
}