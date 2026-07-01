package com.example.fivechef.WebChef.controller;

import com.example.fivechef.WebChef.dto.AnswerDTO;
import com.example.fivechef.WebChef.dto.InquiryDTO;
import com.example.fivechef.WebChef.entity.Inquiry;
import com.example.fivechef.WebChef.entity.User;
import com.example.fivechef.WebChef.service.InquiryService;
import com.example.fivechef.WebChef.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;


@RequestMapping("/inquiry")
@RequiredArgsConstructor
@Controller
public class InquiryController {
    private final InquiryService inquiryService;
    private final UserService userService;


    @GetMapping("/list")
    public String list(
            Model model,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "kw", defaultValue = "") String kw
    ) {
        Page<Inquiry> paging = inquiryService.list(page, kw);
        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);
        return "inquiry/list";
    }

    @GetMapping("/view/{id}")
    public String view(
            Model model,
            @PathVariable("id") Long id,
            AnswerDTO answerDTO
    ) {
        Inquiry inquiry = inquiryService.view(id);
        model.addAttribute("inquiry", inquiry);
        return "inquiry/view";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/chuga")
    public String chuga(
            Model model,
            InquiryDTO inquiryDTO
    ) {
        return "inquiry/chuga";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/sujung/{id}")
    public String sujung(
            Model model,
            @PathVariable("id") Long id,
            InquiryDTO inquiryDTO,
            Principal principal
    ) {
        Inquiry inquiry = inquiryService.view(id);

        if (!inquiry.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }

        inquiryDTO.setId(inquiry.getId());
        inquiryDTO.setSubject(inquiry.getSubject());
        inquiryDTO.setContent(inquiry.getContent());
        inquiryDTO.setCreateDate(inquiry.getCreateDate());

        model.addAttribute("inquiryDTO", inquiryDTO);
        return "communtiy/sujung";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/chugaProc")
    public String chugaProc(
            @Valid InquiryDTO inquiryDTO,
            BindingResult bindingResult,
            Principal principal
    ) {
        if (bindingResult.hasErrors()) {
            return "inquiry/chuga";
        }
        User user = userService.getUserEntity(principal.getName());
        inquiryService.chugaProc(inquiryDTO, user);
        return "redirect:/inquiry/list";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/sujungProc")
    public String sujungProc(
            @Valid InquiryDTO inquiryDTO,
            BindingResult bindingResult,
            Principal principal
    ) {
        if (bindingResult.hasErrors()) {
            return "inquiry/sujung";
        }

        Inquiry inquiry = inquiryService.view(inquiryDTO.getId());
        if (!inquiry.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다. ");
        }
        inquiryDTO.setCreateDate(inquiry.getCreateDate());

        inquiryService.sujungProc(inquiryDTO, inquiry.getAuthor());
        return "redirect:/inquiry/view/" + inquiry.getId();
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/sakje/{id}")
    public String sakje(
            Model model,
            @PathVariable("id") Long id,
            InquiryDTO inquiryDTO,
            Principal principal
    ) {
        Inquiry inquiry = inquiryService.view(id);

        if (!inquiry.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제권한이 없습니다. ");
        }

        inquiryDTO.setId(inquiry.getId());
        inquiryDTO.setSubject(inquiry.getSubject());
        inquiryDTO.setContent(inquiry.getContent());
        inquiryDTO.setCreateDate(inquiry.getCreateDate());

        model.addAttribute("inquiryDTO", inquiryDTO);
        return "inquiry/sakje";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/vote/{id}")
    public String vote(
            @PathVariable("id") Long id,
            Principal principal
    ) {
        Inquiry inquiry = inquiryService.view(id);
        if (inquiry == null) {
            return "redirect:/";
        }

        User user = userService.getUserEntity(principal.getName());
        inquiryService.vote(inquiry, user);
        return "redirect:/inquiry/view/" + id;
    }

}