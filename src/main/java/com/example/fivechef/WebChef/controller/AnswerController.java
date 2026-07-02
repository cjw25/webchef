package com.example.fivechef.WebChef.controller;

import com.example.fivechef.WebChef.dto.AnswerCreateRequest;
import com.example.fivechef.WebChef.dto.AnswerResponse;
import com.example.fivechef.WebChef.dto.AnswerUpdateRequest;
import com.example.fivechef.WebChef.service.AnswerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RequiredArgsConstructor
@Controller
public class AnswerController {

    private final AnswerService answerService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/answer/create")
    public String createAnswer(
            @ModelAttribute AnswerCreateRequest request,
            Principal principal
    ) {
        AnswerResponse answer = answerService.createAnswer(request, principal.getName());

        return "redirect:/community/view/" + request.getCommunityId() + "#answer_" + answer.getId();
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/answer/update/{id}")
    public String updateAnswer(
            @PathVariable("id") Long id,
            @ModelAttribute AnswerUpdateRequest request,
            Principal principal
    ) {
        answerService.updateAnswer(id, request, principal.getName());

        return "redirect:/community/view/" + request.getCommunityId() + "#answer_" + id;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/answer/delete/{id}")
    public String deleteAnswer(
            @PathVariable("id") Long id,
            Principal principal
    ) {
        Long communityId = answerService.deleteAnswer(id, principal.getName());

        return "redirect:/community/view/" + communityId;
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/answer/vote/{id}")
    public String voteAnswer(
            @PathVariable("id") Long id,
            Principal principal
    ) {
        Long communityId = answerService.voteAnswer(id, principal.getName());

        return "redirect:/community/view/" + communityId + "#answer_" + id;
    }
}