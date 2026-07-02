package com.example.fivechef.WebChef.controller;

import com.example.fivechef.WebChef.dto.CourseCreateRequest;
import com.example.fivechef.WebChef.dto.CourseResponse;
import com.example.fivechef.WebChef.dto.CourseUpdateRequest;
import com.example.fivechef.WebChef.entity.CourseCategory;
import com.example.fivechef.WebChef.entity.CourseStatus;
import com.example.fivechef.WebChef.entity.Difficulty;
import com.example.fivechef.WebChef.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RequiredArgsConstructor
@Controller
public class CourseController {

    private final CourseService courseService;

    @GetMapping("/course/list")
    public String list(
            Model model,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        Page<CourseResponse> paging = courseService.getCourses(page, keyword);

        model.addAttribute("paging", paging);
        model.addAttribute("keyword", keyword);

        return "course";
    }

    @GetMapping("/course/detail/{id}")
    public String detail(
            @PathVariable("id") Long id,
            Model model
    ) {
        CourseResponse course = courseService.getCourseResponse(id);
        model.addAttribute("course", course);

        return "course/detail";
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @GetMapping("/course/create")
    public String createPage(Model model) {
        model.addAttribute("request", new CourseCreateRequest());
        model.addAttribute("categories", CourseCategory.values());
        model.addAttribute("difficulties", Difficulty.values());
        model.addAttribute("statuses", CourseStatus.values());

        return "course/create";
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @PostMapping("/course/create")
    public String createCourse(
            @ModelAttribute("request") CourseCreateRequest request,
            Model model,
            Principal principal
    ) {
        try {
            courseService.createCourse(request, principal.getName());
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("categories", CourseCategory.values());
            model.addAttribute("difficulties", Difficulty.values());
            model.addAttribute("statuses", CourseStatus.values());

            return "course/create";
        }

        return "redirect:/course/list";
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @GetMapping("/course/update/{id}")
    public String updatePage(
            @PathVariable("id") Long id,
            Model model
    ) {
        CourseResponse course = courseService.getCourseResponse(id);

        CourseUpdateRequest request = new CourseUpdateRequest();
        request.setTitle(course.getTitle());
        request.setDescription(course.getDescription());
        request.setThumbnailUrl(course.getThumbnailUrl());
        request.setPrice(course.getPrice());
        request.setCategory(course.getCategory());
        request.setDifficulty(course.getDifficulty());
        request.setStatus(course.getStatus());

        model.addAttribute("course", course);
        model.addAttribute("request", request);
        model.addAttribute("categories", CourseCategory.values());
        model.addAttribute("difficulties", Difficulty.values());
        model.addAttribute("statuses", CourseStatus.values());

        return "course/update";
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @PostMapping("/course/update/{id}")
    public String updateCourse(
            @PathVariable("id") Long id,
            @ModelAttribute("request") CourseUpdateRequest request,
            Model model
    ) {
        try {
            courseService.updateCourse(id, request);
        } catch (Exception e) {
            CourseResponse course = courseService.getCourseResponse(id);

            model.addAttribute("course", course);
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("categories", CourseCategory.values());
            model.addAttribute("difficulties", Difficulty.values());
            model.addAttribute("statuses", CourseStatus.values());

            return "course/update";
        }

        return "redirect:/course/detail/" + id;
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @PostMapping("/course/delete/{id}")
    public String deleteCourse(@PathVariable("id") Long id) {
        courseService.deleteCourse(id);
        return "redirect:/course/list";
    }
}