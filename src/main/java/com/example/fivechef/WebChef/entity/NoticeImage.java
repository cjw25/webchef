package com.example.fivechef.WebChef.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "notice_images")
public class NoticeImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imageUrl;

    private String storedFileName;

    private String originalFileName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id")
    private Notice notice;

    public NoticeImage(
            String imageUrl,
            String storedFileName,
            String originalFileName
    ) {
        this.imageUrl = imageUrl;
        this.storedFileName = storedFileName;
        this.originalFileName = originalFileName;
    }

    public void setNotice(Notice notice) {
        this.notice = notice;
    }
}