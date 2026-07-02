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

    // =========================
    // 화면용
    // =========================

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
            model.addAttribute("request", request);
            return "create";
        }

        return "redirect:/user/login";
    }

    @GetMapping("/user/find-id")
    public String findIdPage(Model model) {
        model.addAttribute("request", new FindIdRequest());
        return "find-id";
    }

    @PostMapping("/user/find-id")
    public String findId(
            @ModelAttribute("request") FindIdRequest request,
            Model model
    ) {
        try {
            String username = userService.findUsername(
                    request.getName(),
                    request.getEmail()
            );

            model.addAttribute("foundUsername", username);
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
        }

        return "find-id";
    }

    @GetMapping("/user/find-password")
    public String findPasswordPage(Model model) {
        model.addAttribute("request", new FindPasswordRequest());
        return "find-password";
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

        return "find-password";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/user/list")
    public String list(
            Model model,
            @RequestParam(value = "page", defaultValue = "0") int page
    ) {
        Page<UserResponse> paging = userService.getUsers(page);

        model.addAttribute("paging", paging);

        return "list";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/user/view/{id}")
    public String view(
            Model model,
            @PathVariable("id") Long id
    ) {
        UserResponse user = userService.getUserResponse(id);

        model.addAttribute("user", user);

        return "view";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/user/update/{id}")
    public String updatePage(
            Model model,
            @PathVariable("id") Long id
    ) {
        UserResponse user = userService.getUserResponse(id);

        UserUpdateRequest request = new UserUpdateRequest();
        request.setName(user.getName());
        request.setEmail(user.getEmail());

        model.addAttribute("user", user);
        model.addAttribute("request", request);

        return "update";
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
            model.addAttribute("request", request);
            model.addAttribute("errorMessage", e.getMessage());

            return "update";
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

    // =========================
    // API용
    // =========================

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
    @PostMapping("/api/users/find-id")
    public Map<String, Object> apiFindId(@RequestBody FindIdRequest request) {
        String username = userService.findUsername(
                request.getName(),
                request.getEmail()
        );

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("username", username);

        return result;
    }

    @ResponseBody
    @PostMapping("/api/users/reset-password")
    public Map<String, Object> apiResetPassword(@RequestBody FindPasswordRequest request) {
        String temporaryPassword = userService.resetPassword(
                request.getUsername(),
                request.getEmail()
        );

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("temporaryPassword", temporaryPassword);

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

    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/api/users/{id}")
    public Map<String, Object> apiUpdate(
            @PathVariable("id") Long id,
            @RequestBody UserUpdateRequest request
    ) {
        userService.updateUser(id, request);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "회원 정보가 수정되었습니다.");

        return result;
    }

    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/api/users/{id}")
    public Map<String, Object> apiDelete(@PathVariable("id") Long id) {
        userService.deleteUser(id);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "회원이 삭제되었습니다.");

        return result;
    }

    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/api/users/{id}/role")
    public Map<String, Object> apiChangeRole(
            @PathVariable("id") Long id,
            @RequestParam("role") Role role
    ) {
        userService.changeRole(id, role);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "권한이 변경되었습니다.");

        return result;
    }
}