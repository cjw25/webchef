package com.example.fivechef.WebChef.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InquiryAnswerRequest {

    @NotNull(message = "문의 번호가 필요합니다.")
    private Long inquiryId;

    @NotBlank(message = "답변 내용을 입력하세요.")
    @Size(max = 1000, message = "답변은 1000자 이내로 입력해주세요.")
    private String content;
}
