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
    @GetMapping("/course/{courseId}/quiz/create")
    public String createOrEditQuizPage(
            @PathVariable("courseId") Long courseId,
            Model model
    ) {
        boolean hasQuiz = quizService.getQuizByCourseId(courseId).isPresent();

        QuizCreateRequest request = hasQuiz
                ? quizService.getQuizEditRequest(courseId)
                : new QuizCreateRequest();
        request.setCourseId(courseId);

        model.addAttribute("request", request);
        model.addAttribute("courseId", courseId);
        model.addAttribute("isEdit", hasQuiz);

        return "course/quiz-create";
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @PostMapping("/course/quiz/create")
    public String createOrUpdateQuiz(
            @ModelAttribute QuizCreateRequest request,
            Model model
    ) {
        try {
            if (quizService.getQuizByCourseId(request.getCourseId()).isPresent()) {
                quizService.updateQuiz(request.getCourseId(), request);
            } else {
                quizService.createQuiz(request);
            }
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("request", request);
            model.addAttribute("courseId", request.getCourseId());
            return "course/quiz-create";
        }

        return "redirect:/course/view/" + request.getCourseId();
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/course/{courseId}/quiz")
    public String quizPage(
            @PathVariable("courseId") Long courseId,
            Model model
    ) {
        Optional<QuizResponse> quiz = quizService.getQuizByCourseId(courseId);

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