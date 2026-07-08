package com.example.fivechef.WebChef.service;

import com.example.fivechef.WebChef.dto.InstructorRequest;
import com.example.fivechef.WebChef.dto.InstructorResponse;
import com.example.fivechef.WebChef.entity.Instructor;
import com.example.fivechef.WebChef.entity.InstructorStatus;
import com.example.fivechef.WebChef.entity.Role;
import com.example.fivechef.WebChef.entity.User;
import com.example.fivechef.WebChef.repository.InstructorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class InstructorService {

    private final InstructorRepository instructorRepository;

    private final UserService userService;

    @Transactional
    public void createInstructor(String username, InstructorRequest request) {
        validateRequest(request);

        User user = userService.getLoginUserEntity(username);

        if (user.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException("관리자는 강사 신청 대상이 아닙니다.");
        }

        if (user.getRole() == Role.INSTRUCTOR) {
            throw new IllegalArgumentException("이미 강사 권한을 가지고 있습니다.");
        }

        Instructor instructor = instructorRepository.findByUser(user)
                .orElseGet(Instructor::new);

        if (instructor.getId() != null && instructor.getStatus() == InstructorStatus.PENDING) {
            throw new IllegalArgumentException("이미 승인 대기 중인 강사 신청이 있습니다.");
        }

        if (instructor.getId() != null && instructor.getStatus() == InstructorStatus.APPROVED) {
            throw new IllegalArgumentException("이미 승인된 강사입니다.");
        }

        instructor.setUser(user);
        instructor.setSpecialty(request.getSpecialty().trim());
        instructor.setIntroduction(request.getIntroduction().trim());
        instructor.setCareer(clean(request.getCareer()));
        instructor.setPortfolioUrl(clean(request.getPortfolioUrl()));
        instructor.setStatus(InstructorStatus.PENDING);
        instructor.setRejectReason(null);
        instructor.setReviewedAt(null);

        if (instructor.getId() != null) {
            instructor.setCreatedAt(LocalDateTime.now());
        }

        instructorRepository.save(instructor);
    }

    @Transactional(readOnly = true)
    public InstructorResponse getMyInstructor(String username) {
        User user = userService.getLoginUserEntity(username);

        return instructorRepository.findByUser(user)
                .map(InstructorResponse::new)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public InstructorResponse getInstructor(Long id) {
        Instructor instructor = getInstructorEntity(id);
        return new InstructorResponse(instructor);
    }

    @Transactional(readOnly = true)
    public Page<InstructorResponse> getInstructors(int page, String status) {
        Pageable pageable = PageRequest.of(
                page,
                10,
                Sort.by(Sort.Order.desc("id"))
        );

        InstructorStatus parsedStatus = parseStatus(status);

        if (parsedStatus == null) {
            return instructorRepository.findAll(pageable)
                    .map(InstructorResponse::new);
        }

        return instructorRepository.findByStatus(parsedStatus, pageable)
                .map(InstructorResponse::new);
    }

    @Transactional
    public void approveInstructor(Long id) {
        Instructor instructor = getInstructorEntity(id);

        if (instructor.getStatus() != InstructorStatus.PENDING) {
            throw new IllegalArgumentException("이미 처리된 강사 신청입니다.");
        }

        User user = instructor.getUser();

        if (user == null) {
            throw new IllegalArgumentException("신청자 정보가 없습니다.");
        }

        user.setRole(Role.INSTRUCTOR);

        instructor.setStatus(InstructorStatus.APPROVED);
        instructor.setRejectReason(null);
        instructor.setReviewedAt(LocalDateTime.now());

        instructorRepository.save(instructor);
    }

    @Transactional
    public void rejectInstructor(Long id, String rejectReason) {
        Instructor instructor = getInstructorEntity(id);

        if (instructor.getStatus() != InstructorStatus.PENDING) {
            throw new IllegalArgumentException("이미 처리된 강사 신청입니다.");
        }

        if (isBlank(rejectReason)) {
            throw new IllegalArgumentException("반려 사유를 입력해주세요.");
        }

        instructor.setStatus(InstructorStatus.REJECTED);
        instructor.setRejectReason(rejectReason.trim());
        instructor.setReviewedAt(LocalDateTime.now());

        instructorRepository.save(instructor);
    }

    @Transactional(readOnly = true)
    public long getPendingCount() {
        return instructorRepository.countByStatus(InstructorStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public InstructorResponse getInstructorDashboard(String username) {
        User user = userService.getLoginUserEntity(username);

        if (user.getRole() != Role.INSTRUCTOR && user.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("강사 권한이 없습니다.");
        }

        return instructorRepository.findByUser(user)
                .map(InstructorResponse::new)
                .orElseGet(() -> new InstructorResponse(user));
    }

    private Instructor getInstructorEntity(Long id) {
        return instructorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("강사 정보를 찾을 수 없습니다."));
    }

    private InstructorStatus parseStatus(String status) {
        if (isBlank(status)) {
            return null;
        }

        try {
            return InstructorStatus.valueOf(status.trim().toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

    private void validateRequest(InstructorRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("강사 신청 정보가 없습니다.");
        }

        if (isBlank(request.getSpecialty())) {
            throw new IllegalArgumentException("전문 분야를 입력해주세요.");
        }

        if (isBlank(request.getIntroduction())) {
            throw new IllegalArgumentException("자기소개를 입력해주세요.");
        }
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();

        if (trimmed.isEmpty()) {
            return null;
        }

        return trimmed;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}