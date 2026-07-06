package com.example.fivechef.WebChef.controller;

import com.example.fivechef.WebChef.dto.FridgeSearchResponse;
import com.example.fivechef.WebChef.service.FridgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequiredArgsConstructor
@Controller
public class FridgeController {

    private final FridgeService fridgeService;

    @GetMapping("/fridge")
    public String fridge(Model model) {
        FridgeSearchResponse searchResult = fridgeService.getEmptyResult();

        model.addAttribute("keyword", "");
        model.addAttribute("searchResult", searchResult);

        return "fridge/index";
    }

    @GetMapping("/fridge/search")
    public String search(
            @RequestParam(value = "keyword", required = false) String keyword,
            Model model
    ) {
        FridgeSearchResponse searchResult = fridgeService.search(keyword);

        model.addAttribute("keyword", searchResult.getKeyword());
        model.addAttribute("searchResult", searchResult);

        return "fridge/index";
    }
}