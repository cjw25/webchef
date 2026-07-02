package com.example.fivechef.WebChef.controller;

import com.example.fivechef.WebChef.dto.CourseResponse;
import com.example.fivechef.WebChef.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@RequiredArgsConstructor
@Controller
public class MainController {

    private final CourseService courseService;

    @GetMapping({"/", "/index"})
    public String index(Model model) {

        Page<CourseResponse> coursePage = courseService.getOpenCourses(0);
        List<CourseResponse> popularCourses = coursePage.getContent();

        model.addAttribute("popularCourses", popularCourses);

        return "index";
    }
}