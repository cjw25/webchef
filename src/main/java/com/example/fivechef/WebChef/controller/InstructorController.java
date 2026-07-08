package com.example.fivechef.WebChef.controller;

import com.example.fivechef.WebChef.dto.InstructorRequest;
import com.example.fivechef.WebChef.dto.InstructorResponse;
import com.example.fivechef.WebChef.service.InstructorService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RequiredArgsConstructor
@Controller
public class InstructorController {

    private final InstructorService instructorService;

    // 일반 USER가 강사 신청하는 페이지
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/instructor/create")
    public String createInstructorPage(Model model, Principal principal) {
        InstructorResponse instructor =
                instructorService.getMyInstructor(principal.getName());

        model.addAttribute("request", new InstructorRequest());
        model.addAttribute("instructor", instructor);

        return "instructor/create";
    }

    // 일반 USER가 강사 신청 제출
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/instructor/create")
    public String createInstructor(
            @ModelAttribute("request") InstructorRequest request,
            Model model,
            Principal principal
    ) {
        try {
            instructorService.createInstructor(principal.getName(), request);
        } catch (Exception e) {
            InstructorResponse instructor =
                    instructorService.getMyInstructor(principal.getName());

            model.addAttribute("instructor", instructor);
            model.addAttribute("errorMessage", e.getMessage());

            return "instructor/create";
        }

        return "redirect:/instructor/status";
    }

    // 일반 USER가 본인 강사 신청 상태 확인
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/instructor/status")
    public String instructorStatus(Model model, Principal principal) {
        InstructorResponse instructor =
                instructorService.getMyInstructor(principal.getName());

        model.addAttribute("instructor", instructor);

        return "instructor/status";
    }

    // 승인된 강사 대시보드
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @GetMapping("/instructor")
    public String dashboard(Model model, Principal principal) {
        model.addAttribute(
                "instructor",
                instructorService.getInstructorDashboard(principal.getName())
        );

        return "instructor/dashboard";
    }

    // 강사 강의 관리
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @GetMapping("/instructor/courses")
    public String courses(Model model, Principal principal) {
        model.addAttribute(
                "instructor",
                instructorService.getInstructorDashboard(principal.getName())
        );

        return "instructor/courses";
    }

    // 강사 강의 등록 화면
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @GetMapping("/instructor/courses/create")
    public String createCoursePage(Model model, Principal principal) {
        model.addAttribute(
                "instructor",
                instructorService.getInstructorDashboard(principal.getName())
        );

        return "instructor/course-create";
    }
}