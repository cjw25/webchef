package com.example.fivechef.WebChef.dto;

import com.example.fivechef.WebChef.entity.Answer;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AnswerResponse {

    private final Long id;
    private final String content;

    private final Long communityId;

    private final Long authorId;
    private final String authorName;
    private final String authorUsername;

    private final int voterCount;

    private final LocalDateTime createDate;
    private final LocalDateTime modifyDate;

    public AnswerResponse(Answer answer) {
        this.id = answer.getId();
        this.content = answer.getContent();

        this.communityId = answer.getCommunity() == null ? null : answer.getCommunity().getId();

        if (answer.getAuthor() != null) {
            this.authorId = answer.getAuthor().getId();
            this.authorName = answer.getAuthor().getName();
            this.authorUsername = answer.getAuthor().getUsername();
        } else {
            this.authorId = null;
            this.authorName = null;
            this.authorUsername = null;
        }

        this.voterCount = answer.getVoter() == null ? 0 : answer.getVoter().size();

        this.createDate = answer.getCreateDate();
        this.modifyDate = answer.getModifyDate();
    }
}