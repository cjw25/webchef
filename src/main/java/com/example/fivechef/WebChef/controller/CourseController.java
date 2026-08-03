package com.example.fivechef.WebChef.controller;

import com.example.fivechef.WebChef.dto.CourseResponse;
import com.example.fivechef.WebChef.entity.CourseCategory;
import com.example.fivechef.WebChef.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.example.fivechef.WebChef.service.CourseSessionService;
import com.example.fivechef.WebChef.service.CourseCommentService;
import com.example.fivechef.WebChef.service.UserService;
import com.example.fivechef.WebChef.service.QuizService;

import java.security.Principal;

@RequiredArgsConstructor
@Controller
public class CourseController {

    private final CourseService courseService;
    private final CourseSessionService courseSessionService;
    private final CourseCommentService courseCommentService;
    private final UserService userService;
    private final QuizService quizService;

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
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model,
            Principal principal
    ) {
        String username = principal == null ? null : principal.getName();

        CourseResponse course;

        try {
            course = courseService.getCourseDetailResponse(id, username);
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("redirectUrl",
                    principal == null ? "/user/login" : "/payment/course/" + id);
            return "course/access-denied";
        }

        model.addAttribute("course", course);

        model.addAttribute("relatedCourses", courseService.getRelatedCourses(id, course.getCategory()));
        model.addAttribute("sessions", courseSessionService.getSessions(id));
        model.addAttribute("comments", courseCommentService.getComments(id, page));
        model.addAttribute("commentPage", page);
        model.addAttribute("hasQuiz", quizService.getQuizByCourseId(id).isPresent());

        boolean isLogin = principal != null;
        model.addAttribute("isLogin", isLogin);

        if (isLogin) {
            model.addAttribute("loginUsername", username);
            model.addAttribute("loginRole", userService.getLoginUserEntity(username).getRole().name());
        } else {
            model.addAttribute("loginUsername", null);
            model.addAttribute("loginRole", null);
        }

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