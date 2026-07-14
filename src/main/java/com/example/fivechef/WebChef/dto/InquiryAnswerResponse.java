package com.example.fivechef.WebChef.dto;

import com.example.fivechef.WebChef.entity.InquiryAnswer;
import com.example.fivechef.WebChef.entity.Role;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class InquiryAnswerResponse {

    private final Long id;
    private final String content;
    private final Long authorId;
    private final String authorName;
    private final LocalDateTime createDate;
    private final boolean companyAnswer;

    public InquiryAnswerResponse(InquiryAnswer answer) {
        this.id = answer.getId();
        this.content = answer.getContent();
        this.authorId = answer.getAuthor().getId();
        this.authorName = answer.getAuthor().getUsername();
        this.createDate = answer.getCreateDate();
        this.companyAnswer = answer.getAuthor().getRole() == Role.ADMIN;
    }
}
