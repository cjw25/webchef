package com.example.fivechef.WebChef.controller;

import com.example.fivechef.WebChef.service.CoursePaymentPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@Controller
public class CoursePaymentController {

    private final CoursePaymentPageService coursePaymentPageService;

    @GetMapping("/payment/course/{courseId}")
    public String coursePaymentPage(
            @PathVariable Long courseId,
            Model model
    ) {
        model.addAttribute(
                "paymentPage",
                coursePaymentPageService.getCoursePaymentPage(courseId)
        );

        return "payment/course";
    }
}