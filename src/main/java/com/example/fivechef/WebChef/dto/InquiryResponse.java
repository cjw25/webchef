package com.example.fivechef.WebChef.dto;

import com.example.fivechef.WebChef.entity.Inquiry;
import lombok.Getter;

import java.time.LocalDateTime;

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
    }
}