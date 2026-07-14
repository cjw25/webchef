package com.example.fivechef.WebChef.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class InquiryUpdateRequest {

    private String subject;

    private String content;

    private String answerContent;

//    private List<Long> deleteImageIds;

    private List<Long> deleteImageIds = new ArrayList<>();
}