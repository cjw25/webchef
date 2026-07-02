package com.example.fivechef.WebChef.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "inquiries")
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 문의 제목
    @Column(nullable = false, length = 200)
    private String subject;

    // 문의 내용
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    // 문의 작성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    // 관리자 답변 내용
    @Column(columnDefinition = "TEXT")
    private String answerContent;

    // 답변 여부
    @Column(nullable = false)
    private Boolean answered = false;

    private LocalDateTime createDate;

    private LocalDateTime modifyDate;

    private LocalDateTime answerDate;

    @PrePersist
    public void prePersist() {
        this.createDate = LocalDateTime.now();

        if (this.answered == null) {
            this.answered = false;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.modifyDate = LocalDateTime.now();
    }
}