package com.example.fivechef.WebChef.controller;

import com.example.fivechef.WebChef.dto.TipResponse;
import com.example.fivechef.WebChef.entity.TipCategory;
import com.example.fivechef.WebChef.service.TipsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/tips/create")
    public String createForm(Model model) {
        model.addAttribute("categories", TipCategory.values());

        return "tips/create";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/tips/create")
    public String create(
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam TipCategory category,
            @RequestParam(required = false) MultipartFile imageFile
    ) {
        Long id = tipsService.createTip(title, content, category, imageFile);

        return "redirect:/tips/list";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/tips/update/{id}")
    public String updateForm(
            @PathVariable Long id,
            Model model
    ) {
        TipResponse tip =
                tipsService.getTipForUpdate(id);

        model.addAttribute("tip", tip);
        model.addAttribute(
                "categories",
                TipCategory.values()
        );

        return "tips/update";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/tips/update/{id}")
    public String update(
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam TipCategory category,
            @RequestParam(required = false)
            MultipartFile imageFile,
            @RequestParam(
                    value = "deleteImage",
                    defaultValue = "false"
            )
            boolean deleteImage
    ) {
        tipsService.updateTip(
                id,
                title,
                content,
                category,
                imageFile,
                deleteImage
        );

        return "redirect:/tips/view/" + id;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/tips/delete/{id}")
    public String delete(@PathVariable Long id) {
        tipsService.deleteTip(id);

        return "redirect:/tips/list";
    }
}