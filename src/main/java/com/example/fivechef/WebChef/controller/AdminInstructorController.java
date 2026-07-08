package com.example.fivechef.WebChef.controller;

import com.example.fivechef.WebChef.dto.InstructorResponse;
import com.example.fivechef.WebChef.service.InstructorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@Controller
@RequestMapping("/admin/instructors/check")
@PreAuthorize("hasRole('ADMIN')")
public class AdminInstructorController {

    private final InstructorService instructorService;

    // 강사 신청 목록
    @GetMapping
    public String list(
            Model model,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "status", required = false) String status
    ) {
        Page<InstructorResponse> paging =
                instructorService.getInstructors(page, status);

        model.addAttribute("paging", paging);
        model.addAttribute("status", status);

        return "admin/instructor-check";
    }

    // 강사 신청 상세
    @GetMapping("/{id}")
    public String detail(
            @PathVariable("id") Long id,
            Model model
    ) {
        InstructorResponse instructor =
                instructorService.getInstructor(id);

        model.addAttribute("instructor", instructor);

        return "admin/instructor-check-detail";
    }

    // 강사 승인
    @PostMapping("/{id}/approve")
    public String approve(@PathVariable("id") Long id) {
        instructorService.approveInstructor(id);

        return "redirect:/admin/instructors/check/" + id;
    }

    // 강사 반려
    @PostMapping("/{id}/reject")
    public String reject(
            @PathVariable("id") Long id,
            @RequestParam(value = "rejectReason", required = false) String rejectReason,
            Model model
    ) {
        try {
            instructorService.rejectInstructor(id, rejectReason);
        } catch (Exception e) {
            InstructorResponse instructor =
                    instructorService.getInstructor(id);

            model.addAttribute("instructor", instructor);
            model.addAttribute("errorMessage", e.getMessage());

            return "admin/instructor-check-detail";
        }

        return "redirect:/admin/instructors/check/" + id;
    }
}