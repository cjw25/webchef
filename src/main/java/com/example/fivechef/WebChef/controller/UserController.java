package com.example.fivechef.WebChef.controller;

import com.example.fivechef.WebChef.dto.FindIdRequest;
import com.example.fivechef.WebChef.dto.FindPasswordRequest;
import com.example.fivechef.WebChef.dto.UserCreateRequest;
import com.example.fivechef.WebChef.dto.UserResponse;
import com.example.fivechef.WebChef.dto.UserUpdateRequest;
import com.example.fivechef.WebChef.entity.Role;
import com.example.fivechef.WebChef.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Controller
public class UserController {

    private final UserService userService;

    @GetMapping("/user/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/user/create")
    public String createPage(Model model) {
        model.addAttribute("request", new UserCreateRequest());
        return "create";
    }

    @PostMapping("/user/create")
    public String createUser(
            @ModelAttribute("request") UserCreateRequest request,
            Model model
    ) {
        try {
            userService.createUser(request);
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "user/create";
        }

        return "redirect:/user/login";
    }

    @GetMapping("/user/find-id")
    public String findIdPage(Model model) {
        model.addAttribute("request", new FindIdRequest());
        return "user/find-id";
    }

    @PostMapping("/user/find-id")
    public String findId(
            @ModelAttribute("request") FindIdRequest request,
            Model model
    ) {
        try {
            String username = userService.findUsername(request.getName(), request.getEmail());
            model.addAttribute("foundUsername", username);
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
        }

        return "user/find-id";
    }

    @GetMapping("/user/find-password")
    public String findPasswordPage(Model model) {
        model.addAttribute("request", new FindPasswordRequest());
        return "user/find-password";
    }

    @PostMapping("/user/find-password")
    public String findPassword(
            @ModelAttribute("request") FindPasswordRequest request,
            Model model
    ) {
        try {
            String temporaryPassword = userService.resetPassword(
                    request.getUsername(),
                    request.getEmail()
            );

            model.addAttribute("temporaryPassword", temporaryPassword);
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
        }

        return "user/find-password";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/user/list")
    public String list(
            Model model,
            @RequestParam(value = "page", defaultValue = "0") int page
    ) {
        Page<UserResponse> paging = userService.getUsers(page);
        model.addAttribute("paging", paging);

        return "user/list";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/user/view/{id}")
    public String view(
            @PathVariable("id") Long id,
            Model model
    ) {
        UserResponse user = userService.getUserResponse(id);
        model.addAttribute("user", user);

        return "user/view";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/user/update/{id}")
    public String updatePage(
            @PathVariable("id") Long id,
            Model model
    ) {
        UserResponse user = userService.getUserResponse(id);

        UserUpdateRequest request = new UserUpdateRequest();
        request.setName(user.getName());
        request.setEmail(user.getEmail());

        model.addAttribute("user", user);
        model.addAttribute("request", request);

        return "user/update";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/user/update/{id}")
    public String updateUser(
            @PathVariable("id") Long id,
            @ModelAttribute("request") UserUpdateRequest request,
            Model model
    ) {
        try {
            userService.updateUser(id, request);
        } catch (Exception e) {
            UserResponse user = userService.getUserResponse(id);

            model.addAttribute("user", user);
            model.addAttribute("errorMessage", e.getMessage());

            return "user/update";
        }

        return "redirect:/user/view/" + id;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/user/delete/{id}")
    public String deleteUser(@PathVariable("id") Long id) {
        userService.deleteUser(id);
        return "redirect:/user/list";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/user/role/{id}")
    public String changeRole(
            @PathVariable("id") Long id,
            @RequestParam("role") Role role
    ) {
        userService.changeRole(id, role);
        return "redirect:/user/view/" + id;
    }

    @ResponseBody
    @PostMapping("/api/users/register")
    public Map<String, Object> apiRegister(@RequestBody UserCreateRequest request) {
        userService.createUser(request);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "회원가입이 완료되었습니다.");

        return result;
    }

    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/users")
    public Page<UserResponse> apiUsers(
            @RequestParam(value = "page", defaultValue = "0") int page
    ) {
        return userService.getUsers(page);
    }

    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/users/{id}")
    public UserResponse apiUser(@PathVariable("id") Long id) {
        return userService.getUserResponse(id);
    }
}