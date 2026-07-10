package com.example.fivechef.WebChef.dto;

import com.example.fivechef.WebChef.entity.CourseComment;
import com.example.fivechef.WebChef.entity.CourseCommentImage;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class CourseCommentResponse {

    private final Long id;
    private final String content;

    private final Long authorId;
    private final String authorName;
    private final boolean authorIsInstructor;

    private final LocalDateTime createDate;

    private final List<String> imageUrls;

    public CourseCommentResponse(CourseComment comment) {
        this.id = comment.getId();
        this.content = comment.getContent();

        if (comment.getAuthor() != null) {
            this.authorId = comment.getAuthor().getId();
            this.authorName = comment.getAuthor().getName();
            this.authorIsInstructor = comment.getAuthor().getRole() == com.example.fivechef.WebChef.entity.Role.INSTRUCTOR;
        } else {
            this.authorId = null;
            this.authorName = null;
            this.authorIsInstructor = false;
        }

        this.createDate = comment.getCreateDate();

        List<CourseCommentImage> imgList = comment.getImages();
        if (imgList != null && !imgList.isEmpty()) {
            this.imageUrls = imgList.stream()
                    .map(CourseCommentImage::getFileUrl)
                    .toList();
        } else {
            this.imageUrls = List.of();
        }
    }
}