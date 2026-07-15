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

    private final String imageUrl;

    private final TipCategory category;

    private final Integer viewCount;

    private final LocalDateTime createdAt;

    private final LocalDateTime updatedAt;

    public TipResponse(Tip tip) {
        this.id = tip.getId();
        this.title = tip.getTitle();
        this.content = tip.getContent();
        this.imageUrl = tip.getImageUrl();
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

    /**
     * 카드 뷰의 부제목 자리에 쓸 본문 미리보기.
     * HTML 태그를 제거하고 40자로 잘라서 보여준다.
     */
    public String getContentPreview() {
        if (this.content == null) {
            return "";
        }

        String plain = this.content.replaceAll("<[^>]*>", "").trim();

        return plain.length() > 40 ? plain.substring(0, 40) + "..." : plain;
    }
}