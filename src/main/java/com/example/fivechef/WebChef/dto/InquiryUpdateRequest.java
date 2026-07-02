package com.example.fivechef.WebChef.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InquiryUpdateRequest {

    private String subject;

    private String content;

    private String answerContent;
}