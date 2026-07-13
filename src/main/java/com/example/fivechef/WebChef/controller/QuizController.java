package com.example.fivechef.WebChef.controller;

import com.example.fivechef.WebChef.dto.QuizCreateRequest;
import com.example.fivechef.WebChef.dto.QuizResponse;
import com.example.fivechef.WebChef.dto.QuizResultResponse;
import com.example.fivechef.WebChef.dto.QuizSubmitRequest;
import com.example.fivechef.WebChef.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Optional;

@RequiredArgsConstructor
@Controller
public class QuizController {

    private final QuizService quizService;

    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @GetMapping("/course/session/{sessionId}/quiz/create")
    public String createQuizPage(
            @PathVariable("sessionId") Long sessionId,
            @RequestParam("courseId") Long courseId,
            Model model
    ) {
        QuizCreateRequest request = new QuizCreateRequest();
        request.setSessionId(sessionId);

        model.addAttribute("request", request);
        model.addAttribute("sessionId", sessionId);
        model.addAttribute("courseId", courseId);

        return "course/quiz-create";
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @PostMapping("/course/quiz/create")
    public String createQuiz(
            @ModelAttribute QuizCreateRequest request,
            @RequestParam("courseId") Long courseId,
            Model model
    ) {
        try {
            quizService.createQuiz(request);
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("request", request);
            model.addAttribute("sessionId", request.getSessionId());
            model.addAttribute("courseId", courseId);
            return "course/quiz-create";
        }

        return "redirect:/course/view/" + courseId;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/course/session/{sessionId}/quiz")
    public String quizPage(
            @PathVariable("sessionId") Long sessionId,
            @RequestParam("courseId") Long courseId,
            Model model
    ) {
        Optional<QuizResponse> quiz = quizService.getQuizBySessionId(sessionId);

        if (quiz.isEmpty()) {
            return "redirect:/course/view/" + courseId;
        }

        model.addAttribute("quiz", quiz.get());
        model.addAttribute("courseId", courseId);

        return "course/quiz";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/course/quiz/{quizId}/submit")
    public String submitQuiz(
            @PathVariable("quizId") Long quizId,
            @ModelAttribute QuizSubmitRequest request,
            @RequestParam("courseId") Long courseId,
            Model model,
            Principal principal
    ) {
        QuizResultResponse result = quizService.submitQuiz(quizId, request, principal.getName());

        model.addAttribute("result", result);
        model.addAttribute("courseId", courseId);

        return "course/quiz-result";
    }
}
