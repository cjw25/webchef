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
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Controller
public class CourseController {

    private final CourseService courseService;

    // =========================
    // 화면용
    // =========================

    @GetMapping("/course/list")
    public String list(
            Model model,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        Page<CourseResponse> paging = courseService.getCourses(page, keyword);

        model.addAttribute("paging", paging);
        model.addAttribute("keyword", keyword);

        return "course-list";
    }

    @GetMapping("/course/detail/{id}")
    public String detail(
            Model model,
            @PathVariable("id") Long id
    ) {
        CourseResponse course = courseService.getCourseResponse(id);

        model.addAttribute("course", course);

        return "course-detail";
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @GetMapping("/course/create")
    public String createPage(Model model) {
        model.addAttribute("request", new CourseCreateRequest());
        model.addAttribute("categories", CourseCategory.values());
        model.addAttribute("difficulties", Difficulty.values());
        model.addAttribute("statuses", CourseStatus.values());

        return "course-create";
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
            model.addAttribute("request", request);
            model.addAttribute("categories", CourseCategory.values());
            model.addAttribute("difficulties", Difficulty.values());
            model.addAttribute("statuses", CourseStatus.values());

            return "course-create";
        }

        return "redirect:/course/list";
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @GetMapping("/course/update/{id}")
    public String updatePage(
            Model model,
            @PathVariable("id") Long id
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

        return "course-update";
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
            model.addAttribute("request", request);
            model.addAttribute("categories", CourseCategory.values());
            model.addAttribute("difficulties", Difficulty.values());
            model.addAttribute("statuses", CourseStatus.values());
            model.addAttribute("errorMessage", e.getMessage());

            return "course-update";
        }

        return "redirect:/course/detail/" + id;
    }

    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @PostMapping("/course/delete/{id}")
    public String deleteCourse(@PathVariable("id") Long id) {
        courseService.deleteCourse(id);

        return "redirect:/course/list";
    }

    // =========================
    // API용
    // =========================

    @ResponseBody
    @GetMapping("/api/courses")
    public Page<CourseResponse> apiCourses(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        return courseService.getCourses(page, keyword);
    }

    @ResponseBody
    @GetMapping("/api/courses/{id}")
    public CourseResponse apiCourse(@PathVariable("id") Long id) {
        return courseService.getCourseResponse(id);
    }

    @ResponseBody
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @PostMapping("/api/courses")
    public Map<String, Object> apiCreateCourse(
            @RequestBody CourseCreateRequest request,
            Principal principal
    ) {
        courseService.createCourse(request, principal.getName());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "강의가 등록되었습니다.");

        return result;
    }

    @ResponseBody
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @PutMapping("/api/courses/{id}")
    public Map<String, Object> apiUpdateCourse(
            @PathVariable("id") Long id,
            @RequestBody CourseUpdateRequest request
    ) {
        courseService.updateCourse(id, request);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "강의가 수정되었습니다.");

        return result;
    }

    @ResponseBody
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @DeleteMapping("/api/courses/{id}")
    public Map<String, Object> apiDeleteCourse(@PathVariable("id") Long id) {
        courseService.deleteCourse(id);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "강의가 삭제되었습니다.");

        return result;
    }
}