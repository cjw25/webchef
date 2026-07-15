package com.example.fivechef.WebChef.controller;

import com.example.fivechef.WebChef.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RequiredArgsConstructor
@Controller
@RequestMapping("/subscription")
@PreAuthorize("hasRole('USER')")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping("/my")
    public String mySubscription(
            Model model,
            Principal principal
    ) {
        model.addAttribute(
                "subscription",
                subscriptionService.getMySubscriptionStatus(principal.getName())
        );

        return "subscription/my";
    }

    @PostMapping("/cancel")
    public String cancelSubscription(Principal principal) {
        subscriptionService.cancelMySubscription(principal.getName());

        return "redirect:/subscription/my";
    }
}