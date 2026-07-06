package com.example.fivechef.WebChef.controller;

import com.example.fivechef.WebChef.dto.FindIdRequest;
import com.example.fivechef.WebChef.dto.FindPasswordRequest;
import com.example.fivechef.WebChef.dto.UserCreateRequest;
import com.example.fivechef.WebChef.dto.UserResponse;
import com.example.fivechef.WebChef.dto.UserUpdateRequest;
import com.example.fivechef.WebChef.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Controller
public class UserController {

    private final UserService userService;

    @GetMapping("/user/login")
    public String loginPage() {
        return "user/login";
    }

    @GetMapping("/user/create")
    public String createPage(Model model) {
        model.addAttribute("request", new UserCreateRequest());
        return "user/create";
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

    /*
     * 마이페이지
     * /mypage, /user/mypage 둘 다 접속 가능
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping({"/mypage", "/user/mypage"})
    public String myPage(Model model, Principal principal) {
        UserResponse user = userService.getLoginUserResponse(principal.getName());

        model.addAttribute("user", user);

        return "user/mypage";
    }

    /*
     * 마이페이지 수정 화면
     * /mypage/update, /user/mypage/update 둘 다 접속 가능
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping({"/mypage/update", "/user/mypage/update"})
    public String updateMyPage(Model model, Principal principal) {
        UserResponse user = userService.getLoginUserResponse(principal.getName());

        UserUpdateRequest request = new UserUpdateRequest();
        request.setName(user.getName());
        request.setEmail(user.getEmail());

        model.addAttribute("user", user);
        model.addAttribute("request", request);

        return "user/mypage-update";
    }

    /*
     * 마이페이지 수정 처리
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping({"/mypage/update", "/user/mypage/update"})
    public String updateMyInfo(
            @ModelAttribute("request") UserUpdateRequest request,
            Model model,
            Principal principal
    ) {
        try {
            userService.updateMyInfo(principal.getName(), request);
        } catch (Exception e) {
            UserResponse user = userService.getLoginUserResponse(principal.getName());

            model.addAttribute("user", user);
            model.addAttribute("errorMessage", e.getMessage());

            return "user/mypage-update";
        }

        return "redirect:/mypage";
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
}