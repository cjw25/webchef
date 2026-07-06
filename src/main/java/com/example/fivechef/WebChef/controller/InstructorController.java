package com.example.fivechef.WebChef.controller;

import com.example.fivechef.WebChef.dto.InstructorResponse;
import com.example.fivechef.WebChef.service.InstructorService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@RequiredArgsConstructor
@Controller
@PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
public class InstructorController {

    private final InstructorService instructorService;

    @GetMapping("/instructor")
    public String dashboard(Model model, Principal principal) {
        InstructorResponse instructor = instructorService.getInstructorDashboard(principal.getName());

        model.addAttribute("instructor", instructor);

        return "instructor/dashboard";
    }

    @GetMapping("/instructor/courses")
    public String courses(Model model, Principal principal) {
        InstructorResponse instructor = instructorService.getInstructorDashboard(principal.getName());

        model.addAttribute("instructor", instructor);

        return "instructor/courses";
    }

    @GetMapping("/instructor/courses/create")
    public String createCoursePage(Model model, Principal principal) {
        InstructorResponse instructor = instructorService.getInstructorDashboard(principal.getName());

        model.addAttribute("instructor", instructor);

        return "instructor/course-create";
    }
}