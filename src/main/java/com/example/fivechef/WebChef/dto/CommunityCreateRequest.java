package com.example.fivechef.WebChef.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommunityCreateRequest {

    private String subject;

    private String content;
}