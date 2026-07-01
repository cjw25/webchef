package com.example.fivechef.WebChef.dto;

import com.example.fivechef.WebChef.entity.Community;
import com.example.fivechef.WebChef.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
public class AnswerDTO {

    private Long id;

    @NotEmpty(message="내용은 필수항목입니다.")
    private String content;
    private LocalDateTime createDate;

    private Long communityId;
    private Long UserId;
}
