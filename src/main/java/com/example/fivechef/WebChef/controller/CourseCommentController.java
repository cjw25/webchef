package com.example.fivechef.WebChef.controller;

import com.example.fivechef.WebChef.dto.CourseCommentCreateRequest;
import com.example.fivechef.WebChef.service.CourseCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@RequiredArgsConstructor
@Controller
public class CourseCommentController {

    private final CourseCommentService courseCommentService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/course/comment/create")
    public String createComment(
            @ModelAttribute CourseCommentCreateRequest request,
            @RequestParam(value = "img", required = false) MultipartFile[] img,
            Model model,
            Principal principal
    ) {
        try {
            courseCommentService.createComment(request, principal.getName(), img);
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/course/view/" + request.getCourseId();
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/course/comment/delete/{id}")
    public String deleteComment(
            @PathVariable("id") Long id,
            @RequestParam("courseId") Long courseId,
            Principal principal
    ) {
        courseCommentService.deleteComment(id, principal.getName());
        return "redirect:/course/view/" + courseId;
    }
}
