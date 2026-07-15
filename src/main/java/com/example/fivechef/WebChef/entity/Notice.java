package com.example.fivechef.WebChef.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "notices")
public class Notice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String subject;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    private LocalDateTime createDate;

    private LocalDateTime modifyDate;

    private int viewCount;

    @OneToMany(
            mappedBy = "notice",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("id ASC")
    private List<NoticeImage> images = new ArrayList<>();

    public void addImage(NoticeImage image) {
        images.add(image);
        image.setNotice(this);
    }

    public void removeImage(NoticeImage image) {
        images.remove(image);
        image.setNotice(null);
    }

    @PrePersist
    public void prePersist() {
        this.createDate = LocalDateTime.now();
        this.viewCount = 0;
    }

    @PreUpdate
    public void preUpdate() {
        this.modifyDate = LocalDateTime.now();
    }
}