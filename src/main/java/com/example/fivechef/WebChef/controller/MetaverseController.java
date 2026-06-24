package com.example.fivechef.WebChef.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MetaverseController {

    @GetMapping("/metaverse")
    public String metaversePage() {
        return "metaverse";
    }
}