package com.example.fivechef.WebChef.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnswerCreateRequest {

    private Long communityId;

    private String content;
}