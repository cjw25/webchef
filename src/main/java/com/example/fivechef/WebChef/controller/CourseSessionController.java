package com.example.fivechef.WebChef.controller;

import com.example.fivechef.WebChef.dto.CourseSessionRequest;
import com.example.fivechef.WebChef.service.CourseSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@Controller
public class CourseSessionController {

    private final CourseSessionService courseSessionService;

    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @PostMapping("/course/{courseId}/session/create")
    public String createSession(
            @PathVariable("courseId") Long courseId,
            @ModelAttribute CourseSessionRequest request,
            Model model
    ) {
        try {
            courseSessionService.createSession(courseId, request);
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/course/view/" + courseId;
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @PostMapping("/course/session/update/{id}")
    public String updateSession(
            @PathVariable("id") Long id,
            @ModelAttribute CourseSessionRequest request,
            @RequestParam("courseId") Long courseId
    ) {
        courseSessionService.updateSession(id, request);
        return "redirect:/course/view/" + courseId;
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @PostMapping("/course/session/delete/{id}")
    public String deleteSession(
            @PathVariable("id") Long id,
            @RequestParam("courseId") Long courseId
    ) {
        courseSessionService.deleteSession(id);
        return "redirect:/course/view/" + courseId;
    }
}
