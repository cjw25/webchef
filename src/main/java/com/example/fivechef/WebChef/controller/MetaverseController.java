package com.example.fivechef.WebChef.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MetaverseController {

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/metaverse")
    public String metaversePage() {
        return "metaverse";
    }
}