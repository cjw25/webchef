package com.example.fivechef.WebChef.controller;

import com.example.fivechef.WebChef.dto.AnswerDTO;
import com.example.fivechef.WebChef.dto.CommunityDTO;
import com.example.fivechef.WebChef.entity.Community;
import com.example.fivechef.WebChef.entity.User;
import com.example.fivechef.WebChef.service.CommunityService;
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


@RequestMapping("/community")
@RequiredArgsConstructor
@Controller
public class CommunityController {
    private final CommunityService communityService;
    private final UserService userService;


    @GetMapping("/list")
    public String list(
            Model model,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "kw", defaultValue = "") String kw
    ) {
        Page<Community> paging = communityService.list(page, kw);
        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);
        return "community/list";
    }

    @GetMapping("/view/{id}")
    public String view(
            Model model,
            @PathVariable("id") Long id,
            AnswerDTO answerDTO
    ) {
        Community community = communityService.view(id);
        model.addAttribute("community", community);
        return "community/view";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/chuga")
    public String chuga(
            Model model,
            CommunityDTO communityDTO
    ) {
        return "community/chuga";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/sujung/{id}")
    public String sujung(
            Model model,
            @PathVariable("id") Long id,
            CommunityDTO communityDTO,
            Principal principal
    ) {
        Community community = communityService.view(id);

        if (!community.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다.");
        }

        communityDTO.setId(community.getId());
        communityDTO.setSubject(community.getSubject());
        communityDTO.setContent(community.getContent());
        communityDTO.setCreateDate(community.getCreateDate());

        model.addAttribute("communityDTO", communityDTO);
        return "communtiy/sujung";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/chugaProc")
    public String chugaProc(
            @Valid CommunityDTO communityDTO,
            BindingResult bindingResult,
            Principal principal
    ) {
        if (bindingResult.hasErrors()) {
            return "community/chuga";
        }
        User user = userService.getUserEntity(principal.getName());
        communityService.chugaProc(communityDTO, user);
        return "redirect:/community/list";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/sujungProc")
    public String sujungProc(
            @Valid CommunityDTO communityDTO,
            BindingResult bindingResult,
            Principal principal
    ) {
        if (bindingResult.hasErrors()) {
            return "community/sujung";
        }

        Community community = communityService.view(communityDTO.getId());
        if (!community.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "수정권한이 없습니다. ");
        }
        communityDTO.setCreateDate(community.getCreateDate());

        communityService.sujungProc(communityDTO, community.getAuthor());
        return "redirect:/community/view/" + community.getId();
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/sakje/{id}")
    public String sakje(
            Model model,
            @PathVariable("id") Long id,
            CommunityDTO communityDTO,
            Principal principal
    ) {
        Community community = communityService.view(id);

        if (!community.getAuthor().getUsername().equals(principal.getName())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "삭제권한이 없습니다. ");
        }

        communityDTO.setId(community.getId());
        communityDTO.setSubject(community.getSubject());
        communityDTO.setContent(community.getContent());
        communityDTO.setCreateDate(community.getCreateDate());

        model.addAttribute("communityDTO", communityDTO);
        return "community/sakje";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/vote/{id}")
    public String vote(
            @PathVariable("id") Long id,
            Principal principal
    ) {
        Community community = communityService.view(id);
        if (community == null) {
            return "redirect:/";
        }

        User user = userService.getUserEntity(principal.getName());
        communityService.vote(community, user);
        return "redirect:/community/view/" + id;
    }

}