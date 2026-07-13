package com.example.fivechef.WebChef.controller;

import com.example.fivechef.WebChef.dto.CourseCommentResponse;
import com.example.fivechef.WebChef.dto.CourseCreateRequest;
import com.example.fivechef.WebChef.dto.CourseResponse;
import com.example.fivechef.WebChef.dto.CourseUpdateRequest;
import com.example.fivechef.WebChef.entity.CourseCategory;
import com.example.fivechef.WebChef.entity.CourseStatus;
import com.example.fivechef.WebChef.entity.Difficulty;
import com.example.fivechef.WebChef.service.CourseAccessService;
import com.example.fivechef.WebChef.service.CourseCommentService;
import com.example.fivechef.WebChef.service.CourseService;
import com.example.fivechef.WebChef.service.CourseSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@RequiredArgsConstructor
@Controller
public class CourseController {

    private final CourseService courseService;
    private final CourseSessionService courseSessionService;
    private final CourseCommentService courseCommentService;
    private final CourseAccessService courseAccessService;

    @GetMapping("/course/list")
    public String list(
            Model model,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "category", required = false) CourseCategory category,
            @RequestParam(value = "sort", required = false) String sort,
            Principal principal
    ) {
        Page<CourseResponse> paging = courseService.getCourses(
                page,
                keyword,
                category,
                principal == null ? null : principal.getName(),
                sort
        );

        model.addAttribute("paging", paging);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("categories", CourseCategory.values());
        model.addAttribute("sort", sort);

        return "course/list";
    }

    @GetMapping("/course/view/{id}")
    public String view(
            @PathVariable("id") Long id,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model,
            Principal principal
    ) {
        CourseResponse course = courseService.getCourseDetailResponse(id);
        model.addAttribute("course", course);

        model.addAttribute(
                "courseAccess",
                courseAccessService.getCourseAccess(
                        id,
                        principal == null ? null : principal.getName()
                )
        );

        model.addAttribute("sessions", courseSessionService.getSessions(id));

        Page<CourseCommentResponse> comments = courseCommentService.getComments(id, page);
        model.addAttribute("comments", comments);
        model.addAttribute("commentPage", page);

        return "course/view";
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
            @RequestParam(value = "img", required = false) MultipartFile img,
            @RequestParam(value = "video", required = false) MultipartFile video,
            Model model,
            Principal principal
    ) {
        try {
            courseService.createCourse(request, principal.getName(), img, video);
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
        request.setPrice(course.getPrice());
        request.setCategory(course.getCategory());
        request.setDifficulty(course.getDifficulty());
        request.setStatus(course.getStatus());
        request.setVideoUrl(course.getVideoUrl());

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
            @RequestParam(value = "img", required = false) MultipartFile img,
            @RequestParam(value = "video", required = false) MultipartFile video,
            Model model
    ) {
        try {
            courseService.updateCourse(id, request, img, video);
        } catch (Exception e) {
            CourseResponse course = courseService.getCourseResponse(id);

            model.addAttribute("course", course);
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("categories", CourseCategory.values());
            model.addAttribute("difficulties", Difficulty.values());
            model.addAttribute("statuses", CourseStatus.values());

            return "course/update";
        }

        return "redirect:/course/view/" + id;
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @PostMapping("/course/delete/{id}")
    public String deleteCourse(@PathVariable("id") Long id) {
        courseService.deleteCourse(id);
        return "redirect:/course/list";
    }
}