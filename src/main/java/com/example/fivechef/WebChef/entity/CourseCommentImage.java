package com.example.fivechef.WebChef.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "course_comment_images")
public class CourseCommentImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private CourseComment comment;

    @Column(nullable = false)
    private String storedFileName;

    @Column(nullable = false)
    private String fileUrl;

    private int sortOrder;
}