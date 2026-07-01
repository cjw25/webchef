package com.example.fivechef.WebChef.controller;

import com.example.fivechef.WebChef.dto.CourseListResponse;
import com.example.fivechef.WebChef.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final CourseService courseService;

    @GetMapping("/")
    public String mainPage(Model model) {
        List<CourseListResponse> popularCourses = courseService.getPopularCourseList();

        model.addAttribute("popularCourses", popularCourses);

        return "index";
    }

    @GetMapping("/community")
    public String Page(Model model) {
        return "community";
    }
}