package com.example.fivechef.WebChef.controller;

import com.example.fivechef.WebChef.dto.AdminActiveChangeRequest;
import com.example.fivechef.WebChef.dto.AdminInstructorChangeRequest;
import com.example.fivechef.WebChef.dto.AdminMemberResponse;
import com.example.fivechef.WebChef.dto.AdminMemberUpdateRequest;
import com.example.fivechef.WebChef.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping
    public String dashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/members")
    public String members(
            Model model,
            @RequestParam(value = "page", defaultValue = "0") int page
    ) {
        Page<AdminMemberResponse> paging = adminService.getMembers(page);

        model.addAttribute("paging", paging);

        return "admin/members";
    }

    @GetMapping("/members/{id}")
    public String memberDetail(
            @PathVariable("id") Long id,
            Model model
    ) {
        AdminMemberResponse member = adminService.getMember(id);

        model.addAttribute("member", member);
        model.addAttribute("instructorRequest", new AdminInstructorChangeRequest());
        model.addAttribute("activeRequest", new AdminActiveChangeRequest());

        return "admin/member-detail";
    }

    @GetMapping("/members/{id}/update")
    public String updateMemberPage(
            @PathVariable("id") Long id,
            Model model
    ) {
        AdminMemberResponse member = adminService.getMember(id);

        AdminMemberUpdateRequest request = new AdminMemberUpdateRequest();
        request.setName(member.getName());
        request.setEmail(member.getEmail());

        model.addAttribute("member", member);
        model.addAttribute("request", request);

        return "admin/member-update";
    }

    @PostMapping("/members/{id}/update")
    public String updateMember(
            @PathVariable("id") Long id,
            @ModelAttribute("request") AdminMemberUpdateRequest request,
            Model model
    ) {
        try {
            adminService.updateMember(id, request);
        } catch (Exception e) {
            AdminMemberResponse member = adminService.getMember(id);

            model.addAttribute("member", member);
            model.addAttribute("errorMessage", e.getMessage());

            return "admin/member-update";
        }

        return "redirect:/admin/members/" + id;
    }

    @PostMapping("/members/{id}/delete")
    public String deleteMember(@PathVariable("id") Long id) {
        adminService.deleteMember(id);

        return "redirect:/admin/members";
    }

    @PostMapping("/members/{id}/instructor")
    public String changeInstructor(
            @PathVariable("id") Long id,
            @ModelAttribute AdminInstructorChangeRequest request
    ) {
        adminService.changeInstructor(id, request.isInstructor());

        return "redirect:/admin/members/" + id;
    }

    @PostMapping("/members/{id}/active")
    public String changeActive(
            @PathVariable("id") Long id,
            @ModelAttribute AdminActiveChangeRequest request
    ) {
        adminService.changeActive(id, request.isActive());

        return "redirect:/admin/members/" + id;
    }

    @GetMapping("/instructors")
    public String instructors(
            Model model,
            @RequestParam(value = "page", defaultValue = "0") int page
    ) {
        Page<AdminMemberResponse> paging = adminService.getInstructors(page);

        model.addAttribute("paging", paging);

        return "admin/instructors";
    }
}