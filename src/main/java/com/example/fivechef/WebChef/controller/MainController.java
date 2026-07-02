package com.example.fivechef.WebChef.controller;

import com.example.fivechef.WebChef.dto.CourseResponse;
import com.example.fivechef.WebChef.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@RequiredArgsConstructor
@Controller
public class MainController {

    private final CourseService courseService;

    @GetMapping({"/", "/index"})
    public String index(Model model) {
        Page<CourseResponse> popularCourses = courseService.getOpenCourses(0, 6);

        model.addAttribute("popularCourses", popularCourses.getContent());

        return "index";
    }
}