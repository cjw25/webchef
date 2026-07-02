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
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Controller
public class AnswerController {

    private final AnswerService answerService;

    // =========================
    // 화면용
    // =========================

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

    // =========================
    // API용
    // =========================

    @ResponseBody
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/api/answers")
    public Map<String, Object> apiCreateAnswer(
            @RequestBody AnswerCreateRequest request,
            Principal principal
    ) {
        AnswerResponse answer = answerService.createAnswer(request, principal.getName());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "댓글이 등록되었습니다.");
        result.put("answer", answer);

        return result;
    }

    @ResponseBody
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/api/answers/{id}")
    public Map<String, Object> apiUpdateAnswer(
            @PathVariable("id") Long id,
            @RequestBody AnswerUpdateRequest request,
            Principal principal
    ) {
        answerService.updateAnswer(id, request, principal.getName());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "댓글이 수정되었습니다.");

        return result;
    }

    @ResponseBody
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/api/answers/{id}")
    public Map<String, Object> apiDeleteAnswer(
            @PathVariable("id") Long id,
            Principal principal
    ) {
        answerService.deleteAnswer(id, principal.getName());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "댓글이 삭제되었습니다.");

        return result;
    }
}