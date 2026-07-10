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
    private final String category;

    private final int viewCount;

    private final Long authorId;
    private final String authorName;
    private final String authorUsername;

    private final int answerCount;
    private final int voterCount;

    private final boolean mine;

    private final LocalDateTime createDate;
    private final LocalDateTime modifyDate;

    private final String thumbnailUrl;

    private final List<String> images;

    private final List<AnswerResponse> answers;
    private final List<CommunityImageResponse> imageDetails;

    public CommunityResponse(Community community) {
        this(community, false, null);
    }

    public CommunityResponse(Community community, boolean includeAnswers){
        this(community, includeAnswers, null);
    }

    public boolean hasFile() {
        return this.thumbnailUrl != null && !this.thumbnailUrl.isBlank();
    }

    public CommunityResponse(Community community, boolean includeAnswers, String currentUsername) {
        this.id = community.getId();
        this.subject = community.getSubject();
        this.content = community.getContent();
        this.category = community.getCategory();

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

        this.viewCount = community.getViewCount();

        this.mine = currentUsername != null
                && community.getAuthor() != null
                && currentUsername.equals(community.getAuthor().getUsername());

        List<com.example.fivechef.WebChef.entity.CommunityImage> imgList = community.getImages();

        if (imgList != null && !imgList.isEmpty()){
            this.thumbnailUrl = imgList.stream()
                    .filter(com.example.fivechef.WebChef.entity.CommunityImage::isMain)
                    .findFirst()
                    .map(com.example.fivechef.WebChef.entity.CommunityImage::getFileUrl)
                    .orElse(imgList.get(0).getFileUrl());

            this.images = imgList.stream()
                    .map(com.example.fivechef.WebChef.entity.CommunityImage::getFileUrl)
                    .toList();

            this.imageDetails = imgList.stream()
                    .map(CommunityImageResponse::new)
                    .toList();

        } else {
            this.thumbnailUrl = null;
            this.images = List.of();
            this.imageDetails = List.of();
        }

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