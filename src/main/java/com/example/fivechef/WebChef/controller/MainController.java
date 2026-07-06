package com.example.fivechef.WebChef.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping("/")
    public String index(Model model, Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            model.addAttribute("loginUsername", null);
            return "index";
        }

        String username = authentication.getName();

        if ("anonymousUser".equals(username)) {
            model.addAttribute("loginUsername", null);
            return "index";
        }

        model.addAttribute("loginUsername", username);

        return "index";
    }
}