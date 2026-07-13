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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@RequiredArgsConstructor
@Controller
public class CourseController {

    private final CourseService courseService;

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
        model.addAttribute("sort", sort);
        model.addAttribute("categories", CourseCategory.values());

        return "course/list";
    }

    @GetMapping("/course/detail/{id}")
    public String detailRedirect(@PathVariable("id") Long id) {
        return "redirect:/course/view/" + id;
    }

    @GetMapping("/course/view/{id}")
    public String view(
            @PathVariable("id") Long id,
            Model model
    ) {
        CourseResponse course = courseService.getCourseDetailResponse(id);
        model.addAttribute("course", course);

        return "course/view";
    }

    @GetMapping("/course/create")
    public String createForm(Model model) {
        model.addAttribute("courseCreateRequest", new CourseCreateRequest());
        model.addAttribute("categories", CourseCategory.values());
        model.addAttribute("difficulties", Difficulty.values());
        model.addAttribute("statuses", CourseStatus.values());

        return "course/create";
    }

    @PostMapping("/course/create")
    public String create(
            @ModelAttribute CourseCreateRequest request,
            @RequestParam(value = "img", required = false) MultipartFile img,
            @RequestParam(value = "video", required = false) MultipartFile video,
            Principal principal
    ) {
        if (principal == null) {
            return "redirect:/user/login";
        }

        courseService.createCourse(
                request,
                principal.getName(),
                img,
                video
        );

        return "redirect:/course/list";
    }

    @GetMapping("/course/update/{id}")
    public String updateForm(
            @PathVariable("id") Long id,
            Model model
    ) {
        CourseResponse course = courseService.getCourseResponse(id);

        model.addAttribute("course", course);
        model.addAttribute("courseUpdateRequest", new CourseUpdateRequest());
        model.addAttribute("categories", CourseCategory.values());
        model.addAttribute("difficulties", Difficulty.values());
        model.addAttribute("statuses", CourseStatus.values());

        return "course/update";
    }

    @PostMapping("/course/update/{id}")
    public String update(
            @PathVariable("id") Long id,
            @ModelAttribute CourseUpdateRequest request
    ) {
        courseService.updateCourse(id, request);

        return "redirect:/course/view/" + id;
    }

    @PostMapping("/course/delete/{id}")
    public String delete(@PathVariable("id") Long id) {
        courseService.deleteCourse(id);

        return "redirect:/course/list";
    }
}