package com.example.fivechef.WebChef.dto;

import com.example.fivechef.WebChef.entity.Instructor;
import com.example.fivechef.WebChef.entity.InstructorStatus;
import com.example.fivechef.WebChef.entity.Role;
import com.example.fivechef.WebChef.entity.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class InstructorResponse {

    private final Long id;

    private final Long userId;

    private final String username;

    private final String name;

    private final String email;

    private final Role role;

    private final String specialty;

    private final String introduction;

    private final String career;

    private final String portfolioUrl;

    private final InstructorStatus status;

    private final String rejectReason;

    private final LocalDateTime createdAt;

    private final LocalDateTime reviewedAt;

    // 강사 신청 정보 조회용
    public InstructorResponse(Instructor instructor) {
        User user = instructor.getUser();

        this.id = instructor.getId();

        this.userId = user == null ? null : user.getId();
        this.username = user == null ? null : user.getUsername();
        this.name = user == null ? null : user.getName();
        this.email = user == null ? null : user.getEmail();
        this.role = user == null ? null : user.getRole();

        this.specialty = instructor.getSpecialty();
        this.introduction = instructor.getIntroduction();
        this.career = instructor.getCareer();
        this.portfolioUrl = instructor.getPortfolioUrl();

        this.status = instructor.getStatus();
        this.rejectReason = instructor.getRejectReason();

        this.createdAt = instructor.getCreatedAt();
        this.reviewedAt = instructor.getReviewedAt();
    }

    // 강사 대시보드용
    public InstructorResponse(User user) {
        this.id = null;

        this.userId = user.getId();
        this.username = user.getUsername();
        this.name = user.getName();
        this.email = user.getEmail();
        this.role = user.getRole();

        this.specialty = null;
        this.introduction = null;
        this.career = null;
        this.portfolioUrl = null;

        this.status = null;
        this.rejectReason = null;

        this.createdAt = user.getCreatedAt();
        this.reviewedAt = null;
    }

    public boolean isPending() {
        return this.status == InstructorStatus.PENDING;
    }

    public boolean isApproved() {
        return this.status == InstructorStatus.APPROVED;
    }

    public boolean isRejected() {
        return this.status == InstructorStatus.REJECTED;
    }
}