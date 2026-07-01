package com.example.fivechef.WebChef.dto;

import com.example.fivechef.WebChef.entity.Answer;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
public class CommunityDTO {
    private Long id;

    @NotEmpty(message = "제목은 필수항목입니다.")
    @Size(max=200)
    private String subject;

    @NotEmpty(message="내용은 필수항목입니다.")
    private String content;
    private LocalDateTime createDate;
    private LocalDateTime modifyDate;

    private List<Answer> answerList;

    private Long UserId;
}
