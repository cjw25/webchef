package com.example.fivechef.WebChef.controller;

import com.example.fivechef.WebChef.dto.CourseResponse;
import com.example.fivechef.WebChef.entity.CourseCategory;
import com.example.fivechef.WebChef.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "category", required = false) CourseCategory category,
            @RequestParam(value = "sort", required = false) String sort,
            Principal principal
    ) {
        String username = principal == null ? null : principal.getName();

        Page<CourseResponse> paging = courseService.getCourses(
                page,
                keyword,
                category,
                username,
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
            Model model,
            Principal principal
    ) {
        String username = principal == null ? null : principal.getName();

        CourseResponse course = courseService.getCourseDetailResponse(id, username);

        model.addAttribute("course", course);

        return "course/view";
    }

    @GetMapping("/course/create")
    public String legacyCreateRedirect() {
        return "redirect:/instructor/courses/create";
    }

    @GetMapping("/course/update/{id}")
    public String legacyUpdateRedirect(@PathVariable("id") Long id) {
        return "redirect:/instructor/courses/update/" + id;
    }
}