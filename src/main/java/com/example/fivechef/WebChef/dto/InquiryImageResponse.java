package com.example.fivechef.WebChef.dto;

import com.example.fivechef.WebChef.entity.InquiryImage;
import lombok.Getter;

@Getter
public class InquiryImageResponse {

    private final Long id;
    private final String fileUrl;

    public InquiryImageResponse(InquiryImage image) {
        this.id = image.getId();
        this.fileUrl = image.getFileUrl();
    }
}