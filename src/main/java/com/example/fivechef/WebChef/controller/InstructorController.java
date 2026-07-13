package com.example.fivechef.WebChef.controller;

import com.example.fivechef.WebChef.dto.CourseCreateRequest;
import com.example.fivechef.WebChef.dto.CourseResponse;
import com.example.fivechef.WebChef.dto.CourseUpdateRequest;
import com.example.fivechef.WebChef.dto.InstructorRequest;
import com.example.fivechef.WebChef.dto.InstructorResponse;
import com.example.fivechef.WebChef.entity.CourseCategory;
import com.example.fivechef.WebChef.entity.Difficulty;
import com.example.fivechef.WebChef.service.CourseService;
import com.example.fivechef.WebChef.service.InstructorService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@RequiredArgsConstructor
@Controller
public class InstructorController {

    private final InstructorService instructorService;
    private final CourseService courseService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/instructor/create")
    public String createInstructorPage(Model model, Principal principal) {
        InstructorResponse instructor =
                instructorService.getMyInstructor(principal.getName());

        model.addAttribute("request", new InstructorRequest());
        model.addAttribute("instructor", instructor);

        return "instructor/create";
    }

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

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/instructor/status")
    public String instructorStatus(Model model, Principal principal) {
        InstructorResponse instructor =
                instructorService.getMyInstructor(principal.getName());

        model.addAttribute("instructor", instructor);

        return "instructor/status";
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @GetMapping("/instructor")
    public String dashboard(Model model, Principal principal) {
        model.addAttribute(
                "instructor",
                instructorService.getInstructorDashboard(principal.getName())
        );

        return "instructor/dashboard";
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @GetMapping("/instructor/courses")
    public String courses(Model model, Principal principal) {
        model.addAttribute(
                "instructor",
                instructorService.getInstructorDashboard(principal.getName())
        );

        model.addAttribute(
                "courses",
                courseService.getInstructorCourses(principal.getName())
        );

        return "instructor/courses";
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @GetMapping("/instructor/courses/create")
    public String createCoursePage(Model model, Principal principal) {
        model.addAttribute(
                "instructor",
                instructorService.getInstructorDashboard(principal.getName())
        );

        model.addAttribute("courseCreateRequest", new CourseCreateRequest());
        model.addAttribute("categories", CourseCategory.values());
        model.addAttribute("difficulties", Difficulty.values());

        return "instructor/course-create";
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping("/instructor/courses/create")
    public String createCourse(
            @ModelAttribute CourseCreateRequest request,
            @RequestParam(value = "img", required = false) MultipartFile img,
            Principal principal,
            Model model
    ) {
        try {
            courseService.createCourse(
                    request,
                    principal.getName(),
                    img
            );

            return "redirect:/instructor/courses";

        } catch (Exception e) {
            model.addAttribute(
                    "instructor",
                    instructorService.getInstructorDashboard(principal.getName())
            );
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("courseCreateRequest", request);
            model.addAttribute("categories", CourseCategory.values());
            model.addAttribute("difficulties", Difficulty.values());

            return "instructor/course-create";
        }
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @GetMapping("/instructor/courses/update/{id}")
    public String updateCoursePage(
            @PathVariable("id") Long id,
            Model model,
            Principal principal
    ) {
        CourseResponse course = courseService.getCourseResponse(id);

        CourseUpdateRequest request = new CourseUpdateRequest();
        request.setTitle(course.getTitle());
        request.setDescription(course.getDescription());
        request.setPrice(course.getPrice());
        request.setCategory(course.getCategory());
        request.setDifficulty(course.getDifficulty());
        request.setCookTime(course.getCookTime());
        request.setVideoUrl(course.getVideoUrl());

        model.addAttribute(
                "instructor",
                instructorService.getInstructorDashboard(principal.getName())
        );
        model.addAttribute("course", course);
        model.addAttribute("courseUpdateRequest", request);
        model.addAttribute("categories", CourseCategory.values());
        model.addAttribute("difficulties", Difficulty.values());

        return "instructor/course-update";
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping("/instructor/courses/update/{id}")
    public String updateCourse(
            @PathVariable("id") Long id,
            @ModelAttribute CourseUpdateRequest request,
            @RequestParam(value = "img", required = false) MultipartFile img,
            Principal principal,
            Model model
    ) {
        try {
            courseService.updateCourse(
                    id,
                    request,
                    principal.getName(),
                    img
            );

            return "redirect:/instructor/courses";

        } catch (Exception e) {
            CourseResponse course = courseService.getCourseResponse(id);

            model.addAttribute(
                    "instructor",
                    instructorService.getInstructorDashboard(principal.getName())
            );
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("course", course);
            model.addAttribute("courseUpdateRequest", request);
            model.addAttribute("categories", CourseCategory.values());
            model.addAttribute("difficulties", Difficulty.values());

            return "instructor/course-update";
        }
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PostMapping("/instructor/courses/delete/{id}")
    public String deleteCourse(
            @PathVariable("id") Long id,
            Principal principal
    ) {
        courseService.deleteCourse(id, principal.getName());

        return "redirect:/instructor/courses";
    }
}