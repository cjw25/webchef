package com.example.fivechef.WebChef.dto;

import com.example.fivechef.WebChef.entity.CommunityImage;
import lombok.Getter;

@Getter
public class CommunityImageResponse {

    private final Long id;
    private final String originalFileName;
    private final String fileUrl;
    private final boolean main;

    public CommunityImageResponse(CommunityImage image) {
        this.id = image.getId();
        this.originalFileName = image.getOriginalFileName();
        this.fileUrl = image.getFileUrl();
        this.main = image.isMain();
    }
}
