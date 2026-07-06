package com.example.fivechef.WebChef.controller;

import com.example.fivechef.WebChef.dto.TipResponse;
import com.example.fivechef.WebChef.service.TipsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@Controller
public class TipsController {

    private final TipsService tipsService;

    @GetMapping("/tips/list")
    public String list(
            Model model,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "keyword", defaultValue = "") String keyword,
            @RequestParam(value = "category", required = false) String category
    ) {
        Page<TipResponse> paging = tipsService.getTips(page, keyword, category);

        model.addAttribute("paging", paging);
        model.addAttribute("keyword", keyword);
        model.addAttribute("category", category);

        return "tips/list";
    }

    @GetMapping("/tips/view/{id}")
    public String view(
            @PathVariable("id") Long id,
            Model model
    ) {
        TipResponse tip = tipsService.getTip(id);

        model.addAttribute("tip", tip);

        return "tips/view";
    }
}