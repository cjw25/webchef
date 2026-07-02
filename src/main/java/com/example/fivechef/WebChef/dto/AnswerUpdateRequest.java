package com.example.fivechef.WebChef.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnswerUpdateRequest {

    private Long id;

    private Long communityId;

    private String content;
}