package com.example.fivechef.WebChef.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommunityCreateRequest {

    @NotBlank(message = "태그를 선택해주세요.")
    private String category;

    @NotBlank(message = "제목을 입력해주세요.")
    private String subject;

    @NotBlank(message = "내용을 입력해주세요.")
    private String content;
}