//package com.example.fivechef.WebChef.controller;
//
//import com.example.fivechef.WebChef.dto.AnswerDTO;
//import com.example.fivechef.WebChef.dto.NoticeDTO;
//import com.example.fivechef.WebChef.entity.Notice;
//import com.example.fivechef.WebChef.entity.User;
//import com.example.fivechef.WebChef.service.NoticeService;
//import com.example.fivechef.WebChef.service.UserService;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.Page;
//import org.springframework.http.HttpStatus;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.validation.BindingResult;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.server.ResponseStatusException;
//
//import java.security.Principal;
//
//
//@RequestMapping("/notice")
//@RequiredArgsConstructor
//@Controller
//public class NoticeController {
//    private final NoticeService noticeService;
//    private final UserService userService;
//
//
//    @GetMapping("/list")
//    public String list(
//            Model model,
//            @RequestParam(value = "page", defaultValue = "0") int page,
//            @RequestParam(value = "kw", defaultValue = "") String kw
//    ) {
//        Page<Notice> paging = noticeService.list(page, kw);
//        model.addAttribute("paging", paging);
//        model.addAttribute("kw", kw);
//        return "notice/list";
//    }
//
//    @GetMapping("/view/{id}")
//    public String view(
//            Model model,
//            @PathVariable("id") Long id,
//            AnswerDTO answerDTO
//    ) {
//        Notice notice = noticeService.view(id);
//        model.addAttribute("notice", notice);
//        return "notice/view";
//    }
//
//    @PreAuthorize("isAuthenticated()")
//    @GetMapping("/chuga")
//    public String chuga(
//            Model model,
//            NoticeDTO noticeDTO
//    ) {
//        return "notice/chuga";
//    }
//
//    @PreAuthorize("isAuthenticated()")
//    @GetMapping("/sujung/{id}")
//    public String sujung(
//            Model model,
//            @PathVariable("id") Long id,
//            NoticeDTO noticeDTO,
//            Principal principal
//    ) {
//        Notice notice = noticeService.view(id);
//
//        if (!notice.getAuthor().getUsername().equals(principal.getName())) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
//        }
//
//        noticeDTO.setId(notice.getId());
//        noticeDTO.setSubject(notice.getSubject());
//        noticeDTO.setContent(notice.getContent());
//        noticeDTO.setCreateDate(notice.getCreateDate());
//
//        model.addAttribute("noticeDTO", noticeDTO);
//        return "communtiy/sujung";
//    }
//
//    @PreAuthorize("isAuthenticated()")
//    @PostMapping("/chugaProc")
//    public String chugaProc(
//            @Valid NoticeDTO noticeDTO,
//            BindingResult bindingResult,
//            Principal principal
//    ) {
//        if (bindingResult.hasErrors()) {
//            return "notice/chuga";
//        }
//        User user = userService.getUserEntity(principal.getName());
//        noticeService.chugaProc(noticeDTO, user);
//        return "redirect:/notice/list";
//    }
//
//    @PreAuthorize("isAuthenticated()")
//    @PostMapping("/sujungProc")
//    public String sujungProc(
//            @Valid NoticeDTO noticeDTO,
//            BindingResult bindingResult,
//            Principal principal
//    ) {
//        if (bindingResult.hasErrors()) {
//            return "notice/sujung";
//        }
//
//        Notice notice = noticeService.view(noticeDTO.getId());
//        if (!notice.getAuthor().getUsername().equals(principal.getName())) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다. ");
//        }
//        noticeDTO.setCreateDate(notice.getCreateDate());
//
//        noticeService.sujungProc(noticeDTO, notice.getAuthor());
//        return "redirect:/notice/view/" + notice.getId();
//    }
//
//    @PreAuthorize("isAuthenticated()")
//    @GetMapping("/sakje/{id}")
//    public String sakje(
//            Model model,
//            @PathVariable("id") Long id,
//            NoticeDTO noticeDTO,
//            Principal principal
//    ) {
//        Notice notice = noticeService.view(id);
//
//        if (!notice.getAuthor().getUsername().equals(principal.getName())) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제권한이 없습니다. ");
//        }
//
//        noticeDTO.setId(notice.getId());
//        noticeDTO.setSubject(notice.getSubject());
//        noticeDTO.setContent(notice.getContent());
//        noticeDTO.setCreateDate(notice.getCreateDate());
//
//        model.addAttribute("noticeDTO", noticeDTO);
//        return "notice/sakje";
//    }
//
//    @PreAuthorize("isAuthenticated()")
//    @GetMapping("/vote/{id}")
//    public String vote(
//            @PathVariable("id") Long id,
//            Principal principal
//    ) {
//        Notice notice = noticeService.view(id);
//        if (notice == null) {
//            return "redirect:/";
//        }
//
//        User user = userService.getUserEntity(principal.getName());
//        noticeService.vote(notice, user);
//        return "redirect:/notice/view/" + id;
//    }
//
//}