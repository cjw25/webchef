package com.example.fivechef.WebChef.dto;

import com.example.fivechef.WebChef.entity.NoticeImage;
import lombok.Getter;

@Getter
public class NoticeImageResponse {

    private final Long id;
    private final String imageUrl;

    public NoticeImageResponse(NoticeImage image) {
        this.id = image.getId();
        this.imageUrl = image.getImageUrl();
    }
}