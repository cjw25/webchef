package com.example.fivechef.WebChef.dto;

import com.example.fivechef.WebChef.entity.Tip;
import com.example.fivechef.WebChef.entity.TipCategory;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class TipResponse {

    private final Long id;

    private final String title;

    private final String content;

    private final TipCategory category;

    private final Integer viewCount;

    private final LocalDateTime createdAt;

    private final LocalDateTime updatedAt;

    public TipResponse(Tip tip) {
        this.id = tip.getId();
        this.title = tip.getTitle();
        this.content = tip.getContent();
        this.category = tip.getCategory();
        this.viewCount = tip.getViewCount();
        this.createdAt = tip.getCreatedAt();
        this.updatedAt = tip.getUpdatedAt();
    }

    public String getCategoryName() {
        if (this.category == null) {
            return "";
        }

        return switch (this.category) {
            case INGREDIENT_PREP -> "재료손질법";
            case INGREDIENT_STORE -> "재료보관법";
            case TOOL_MANAGE -> "조리도구 관리법";
        };
    }
}