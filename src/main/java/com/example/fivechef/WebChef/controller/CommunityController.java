package com.example.fivechef.WebChef.controller;

import com.example.fivechef.WebChef.dto.AnswerCreateRequest;
import com.example.fivechef.WebChef.dto.CommunityCreateRequest;
import com.example.fivechef.WebChef.dto.CommunityResponse;
import com.example.fivechef.WebChef.dto.CommunityUpdateRequest;
import com.example.fivechef.WebChef.service.CommunityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RequiredArgsConstructor
@Controller
public class CommunityController {

    private final CommunityService communityService;

    @GetMapping("/community/list")
    public String list(
            Model model,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "kw", required = false) String kw
    ) {
        Page<CommunityResponse> paging = communityService.getCommunities(page, kw);

        model.addAttribute("paging", paging);
        model.addAttribute("kw", kw);

        return "community/list";
    }

    @GetMapping("/community/view/{id}")
    public String view(
            @PathVariable("id") Long id,
            Model model
    ) {
        CommunityResponse community = communityService.getCommunityResponse(id);

        AnswerCreateRequest answerRequest = new AnswerCreateRequest();
        answerRequest.setCommunityId(id);

        model.addAttribute("community", community);
        model.addAttribute("answerRequest", answerRequest);

        return "community/view";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/community/create")
    public String createPage(Model model) {
        model.addAttribute("request", new CommunityCreateRequest());
        return "community/create";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/community/create")
    public String createCommunity(
            @ModelAttribute("request") CommunityCreateRequest request,
            Model model,
            Principal principal
    ) {
        try {
            communityService.createCommunity(request, principal.getName());
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "community/create";
        }

        return "redirect:/community/list";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/community/update/{id}")
    public String updatePage(
            @PathVariable("id") Long id,
            Model model
    ) {
        CommunityResponse community = communityService.getCommunityResponse(id);

        CommunityUpdateRequest request = new CommunityUpdateRequest();
        request.setSubject(community.getSubject());
        request.setContent(community.getContent());

        model.addAttribute("community", community);
        model.addAttribute("request", request);

        return "community/update";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/community/update/{id}")
    public String updateCommunity(
            @PathVariable("id") Long id,
            @ModelAttribute("request") CommunityUpdateRequest request,
            Model model,
            Principal principal
    ) {
        try {
            communityService.updateCommunity(id, request, principal.getName());
        } catch (Exception e) {
            CommunityResponse community = communityService.getCommunityResponse(id);

            model.addAttribute("community", community);
            model.addAttribute("errorMessage", e.getMessage());

            return "community/update";
        }

        return "redirect:/community/view/" + id;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/community/delete/{id}")
    public String deleteCommunity(
            @PathVariable("id") Long id,
            Principal principal
    ) {
        communityService.deleteCommunity(id, principal.getName());
        return "redirect:/community/list";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/community/vote/{id}")
    public String voteCommunity(
            @PathVariable("id") Long id,
            Principal principal
    ) {
        communityService.voteCommunity(id, principal.getName());
        return "redirect:/community/view/" + id;
    }
}