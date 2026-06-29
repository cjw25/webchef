package com.example.fivechef.WebChef.controller;

import com.example.fivechef.WebChef.dto.CourseCreateRequest;
import com.example.fivechef.WebChef.dto.CourseDetailResponse;
import com.example.fivechef.WebChef.dto.CourseListResponse;
import com.example.fivechef.WebChef.dto.CourseUpdateRequest;
import com.example.fivechef.WebChef.entity.Course.CourseCategory;
import com.example.fivechef.WebChef.entity.Course.CourseDifficulty;
import com.example.fivechef.WebChef.entity.Course.CourseStatus;
import com.example.fivechef.WebChef.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/course")
public class CourseController {

    private final CourseService courseService;

    @GetMapping("/list")
    public String courseList(
            @RequestParam(required = false) CourseCategory category,
            @RequestParam(required = false) CourseDifficulty difficulty,
            @RequestParam(required = false) String keyword,
            Model model
    ) {
        List<CourseListResponse> courseList;

        if (keyword != null && !keyword.isBlank()) {
            courseList = courseService.searchCourseList(keyword);
        } else if (category != null) {
            courseList = courseService.getCourseListByCategory(category);
        } else if (difficulty != null) {
            courseList = courseService.getCourseListByDifficulty(difficulty);
        } else {
            courseList = courseService.getOpenCourseList();
        }

        model.addAttribute("courseList", courseList);
        model.addAttribute("categories", CourseCategory.values());
        model.addAttribute("difficulties", CourseDifficulty.values());
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedDifficulty", difficulty);
        model.addAttribute("keyword", keyword);

        return "course/course-list";
    }

    @GetMapping("/{courseId}")
    public String courseDetail(
            @PathVariable Long courseId,
            Model model
    ) {
        CourseDetailResponse course = courseService.getCourseDetail(courseId);

        model.addAttribute("course", course);

        return "course/course-detail";
    }

    @GetMapping("/create")
    public String courseCreateForm(Model model) {
        model.addAttribute("courseCreateRequest", new CourseCreateRequest());
        model.addAttribute("categories", CourseCategory.values());
        model.addAttribute("difficulties", CourseDifficulty.values());
        model.addAttribute("statuses", CourseStatus.values());

        return "course/course-create";
    }

    @PostMapping("/create")
    public String createCourse(
            @ModelAttribute CourseCreateRequest request
    ) {
        Long courseId = courseService.createCourse(request);

        return "redirect:/course/" + courseId;
    }

    @GetMapping("/{courseId}/edit")
    public String courseEditForm(
            @PathVariable Long courseId,
            Model model
    ) {
        CourseDetailResponse course = courseService.getCourseForEdit(courseId);

        CourseUpdateRequest request = new CourseUpdateRequest();
        request.setTitle(course.getTitle());
        request.setSummary(course.getSummary());
        request.setDescription(course.getDescription());
        request.setThumbnailUrl(course.getThumbnailUrl());
        request.setPrice(course.getPrice());
        request.setFree(course.isFree());
        request.setCategory(course.getCategory());
        request.setDifficulty(course.getDifficulty());
        request.setStatus(course.getStatus());

        model.addAttribute("courseId", courseId);
        model.addAttribute("courseUpdateRequest", request);
        model.addAttribute("categories", CourseCategory.values());
        model.addAttribute("difficulties", CourseDifficulty.values());
        model.addAttribute("statuses", CourseStatus.values());

        return "course/course-edit";
    }

    @PostMapping("/{courseId}/edit")
    public String updateCourse(
            @PathVariable Long courseId,
            @ModelAttribute CourseUpdateRequest request
    ) {
        courseService.updateCourse(courseId, request);

        return "redirect:/course/" + courseId;
    }

    @PostMapping("/{courseId}/delete")
    public String deleteCourse(
            @PathVariable Long courseId
    ) {
        courseService.deleteCourse(courseId);

        return "redirect:/course/list";
    }
}