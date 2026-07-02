package com.example.fivechef.WebChef.dto;

import com.example.fivechef.WebChef.entity.Notice;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class NoticeResponse {

    private final Long id;
    private final String subject;
    private final String content;

    private final Long authorId;
    private final String authorName;
    private final String authorUsername;

    private final LocalDateTime createDate;
    private final LocalDateTime modifyDate;

    public NoticeResponse(Notice notice) {
        this.id = notice.getId();
        this.subject = notice.getSubject();
        this.content = notice.getContent();

        if (notice.getAuthor() != null) {
            this.authorId = notice.getAuthor().getId();
            this.authorName = notice.getAuthor().getName();
            this.authorUsername = notice.getAuthor().getUsername();
        } else {
            this.authorId = null;
            this.authorName = null;
            this.authorUsername = null;
        }

        this.createDate = notice.getCreateDate();
        this.modifyDate = notice.getModifyDate();
    }
}