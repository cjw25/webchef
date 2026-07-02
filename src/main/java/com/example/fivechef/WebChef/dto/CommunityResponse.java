package com.example.fivechef.WebChef.dto;

import com.example.fivechef.WebChef.entity.Answer;
import com.example.fivechef.WebChef.entity.Community;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Getter
public class CommunityResponse {

    private final Long id;
    private final String subject;
    private final String content;

    private final Long authorId;
    private final String authorName;
    private final String authorUsername;

    private final int answerCount;
    private final int voterCount;

    private final LocalDateTime createDate;
    private final LocalDateTime modifyDate;

    private final List<AnswerResponse> answers;

    // 목록용
    public CommunityResponse(Community community) {
        this(community, false);
    }

    // 상세용
    public CommunityResponse(Community community, boolean includeAnswers) {
        this.id = community.getId();
        this.subject = community.getSubject();
        this.content = community.getContent();

        if (community.getAuthor() != null) {
            this.authorId = community.getAuthor().getId();
            this.authorName = community.getAuthor().getName();
            this.authorUsername = community.getAuthor().getUsername();
        } else {
            this.authorId = null;
            this.authorName = null;
            this.authorUsername = null;
        }

        this.answerCount = community.getAnswerList() == null ? 0 : community.getAnswerList().size();
        this.voterCount = community.getVoter() == null ? 0 : community.getVoter().size();

        this.createDate = community.getCreateDate();
        this.modifyDate = community.getModifyDate();

        if (includeAnswers && community.getAnswerList() != null) {
            this.answers = community.getAnswerList()
                    .stream()
                    .sorted(Comparator.comparing(Answer::getCreateDate))
                    .map(AnswerResponse::new)
                    .toList();
        } else {
            this.answers = List.of();
        }
    }
}