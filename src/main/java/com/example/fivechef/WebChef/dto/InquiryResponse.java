package com.example.fivechef.WebChef.dto;

import com.example.fivechef.WebChef.entity.Inquiry;
import com.example.fivechef.WebChef.entity.InquiryImage;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class InquiryResponse {

    private final Long id;
    private final String subject;
    private final String content;

    private final Long authorId;
    private final String authorName;
    private final String authorUsername;

    /*
     * 기존 단일 답변 필드
     * 현재 사용 중인 다른 화면이나 기능이 있을 수 있으므로 일단 유지
     */
    private final String answerContent;
    private final Boolean answered;

    private final LocalDateTime createDate;
    private final LocalDateTime modifyDate;
    private final LocalDateTime answerDate;

    private final int viewCount;

    private final List<String> imageUrls;
    private final List<InquiryImageResponse> imageDetails;

    /*
     * 문의 답변 목록
     */
    private final List<InquiryAnswerResponse> answers;

    private boolean mine;

    public InquiryResponse(Inquiry inquiry) {
        this.id = inquiry.getId();
        this.subject = inquiry.getSubject();
        this.content = inquiry.getContent();

        if (inquiry.getAuthor() != null) {
            this.authorId = inquiry.getAuthor().getId();
            this.authorName = inquiry.getAuthor().getName();
            this.authorUsername = inquiry.getAuthor().getUsername();
        } else {
            this.authorId = null;
            this.authorName = null;
            this.authorUsername = null;
        }

        this.answerContent = inquiry.getAnswerContent();
        this.answered = inquiry.getAnswered();

        this.createDate = inquiry.getCreateDate();
        this.modifyDate = inquiry.getModifyDate();
        this.answerDate = inquiry.getAnswerDate();

        this.viewCount = inquiry.getViewCount();

        List<InquiryImage> imgList = inquiry.getImages();

        if (imgList != null && !imgList.isEmpty()) {
            this.imageUrls = imgList.stream()
                    .map(InquiryImage::getFileUrl)
                    .toList();

            this.imageDetails = imgList.stream()
                    .map(InquiryImageResponse::new)
                    .toList();
        } else {
            this.imageUrls = List.of();
            this.imageDetails = List.of();
        }

        /*
         * Inquiry 엔티티에 getAnswers()가 있다는 전제
         */
        if (inquiry.getAnswers() != null && !inquiry.getAnswers().isEmpty()) {
            this.answers = inquiry.getAnswers()
                    .stream()
                    .map(InquiryAnswerResponse::new)
                    .toList();
        } else {
            this.answers = List.of();
        }
    }

    public void setMine(boolean mine) {
        this.mine = mine;
    }
}