package com.example.fivechef.WebChef.dto;

import com.example.fivechef.WebChef.entity.Inquiry;
import com.example.fivechef.WebChef.entity.InquiryImage;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class InquiryResponse {

    private final Long id;
    private final String subject;
    private final String content;

    private final Long authorId;
    private final String authorName;
    private final String authorUsername;

    private final String answerContent;
    private final Boolean answered;

    private final LocalDateTime createDate;
    private final LocalDateTime modifyDate;
    private final LocalDateTime answerDate;
    private final int viewCount;
    private final List<String> imageUrls;

    public InquiryResponse(Inquiry inquiry) {
        this.id = inquiry.getId();
        this.subject = inquiry.getSubject();
        this.content = inquiry.getContent();

        if (inquiry.getAuthor() != null) {
            this.authorId = inquiry.getAuthor().getId();
            this.authorName = inquiry.getAuthor().getName();
            this.authorUsername = inquiry.getAuthor().getUsername();
        } else {
            this.authorId = null;
            this.authorName = null;
            this.authorUsername = null;
        }

        this.answerContent = inquiry.getAnswerContent();
        this.answered = inquiry.getAnswered();

        this.createDate = inquiry.getCreateDate();
        this.modifyDate = inquiry.getModifyDate();
        this.answerDate = inquiry.getAnswerDate();

        this.viewCount = inquiry.getViewCount();

        List<InquiryImage> imgList = inquiry.getImages();
        if (imgList != null && !imgList.isEmpty()) {
            this.imageUrls = imgList.stream()
                    .map(InquiryImage::getFileUrl)
                    .toList();
        } else {
            this.imageUrls = List.of();
        }
    }
}