package com.example.fivechef.WebChef.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
public class CommunityUpdateRequest {

    @NotBlank(message = "태그를 선택해주세요.")
    private String category;

    @NotBlank(message = "제목을 입력해주세요.")
    private String subject;

    @NotBlank(message = "내용을 입력해주세요.")
    private String content;

    private List<Long> deleteImageIds;

    private String mainSelect;

}