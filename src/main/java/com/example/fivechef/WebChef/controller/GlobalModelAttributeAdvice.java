package com.example.fivechef.WebChef.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(annotations = Controller.class)
public class GlobalModelAttributeAdvice {

    @ModelAttribute
    public void addLoginInfo(Model model, Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            setAnonymous(model);
            return;
        }

        String username = authentication.getName();

        if (username == null || "anonymousUser".equals(username)) {
            setAnonymous(model);
            return;
        }

        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);

        boolean isInstructor = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_INSTRUCTOR"::equals);

        String loginRole = "USER";

        if (isAdmin) {
            loginRole = "ADMIN";
        } else if (isInstructor) {
            loginRole = "INSTRUCTOR";
        }

        model.addAttribute("isLogin", true);
        model.addAttribute("loginUsername", username);
        model.addAttribute("loginRole", loginRole);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isInstructor", isInstructor);
    }

    private void setAnonymous(Model model) {
        model.addAttribute("isLogin", false);
        model.addAttribute("loginUsername", null);
        model.addAttribute("loginRole", null);
        model.addAttribute("isAdmin", false);
        model.addAttribute("isInstructor", false);
    }
}