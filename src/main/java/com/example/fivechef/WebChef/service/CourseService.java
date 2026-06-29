package com.example.fivechef.WebChef.service;

import com.example.fivechef.WebChef.dto.CourseCreateRequest;
import com.example.fivechef.WebChef.dto.CourseDetailResponse;
import com.example.fivechef.WebChef.dto.CourseListResponse;
import com.example.fivechef.WebChef.dto.CourseUpdateRequest;
import com.example.fivechef.WebChef.entity.Course;
import com.example.fivechef.WebChef.entity.Course.CourseCategory;
import com.example.fivechef.WebChef.entity.Course.CourseDifficulty;
import com.example.fivechef.WebChef.entity.Course.CourseStatus;
import com.example.fivechef.WebChef.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseService {

    private final CourseRepository courseRepository;

    @Transactional(readOnly = true)
    public List<CourseListResponse> getOpenCourseList() {
        return courseRepository.findByStatusOrderByCreatedAtDesc(CourseStatus.OPEN)
                .stream()
                .map(CourseListResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CourseListResponse> getCourseListByCategory(CourseCategory category) {
        return courseRepository.findByCategoryAndStatusOrderByCreatedAtDesc(
                        category,
                        CourseStatus.OPEN
                )
                .stream()
                .map(CourseListResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CourseListResponse> getCourseListByDifficulty(CourseDifficulty difficulty) {
        return courseRepository.findByDifficultyAndStatusOrderByCreatedAtDesc(
                        difficulty,
                        CourseStatus.OPEN
                )
                .stream()
                .map(CourseListResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CourseListResponse> searchCourseList(String keyword) {
        return courseRepository.searchOpenCourses(keyword, CourseStatus.OPEN)
                .stream()
                .map(CourseListResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CourseListResponse> getPopularCourseList() {
        return courseRepository.findTop4ByStatusOrderByStudentCountDesc(CourseStatus.OPEN)
                .stream()
                .map(CourseListResponse::from)
                .toList();
    }

    public CourseDetailResponse getCourseDetail(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));

        course.setViewCount(course.getViewCount() + 1);

        return CourseDetailResponse.from(course);
    }

    @Transactional(readOnly = true)
    public CourseDetailResponse getCourseForEdit(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));

        return CourseDetailResponse.from(course);
    }

    public Long createCourse(CourseCreateRequest request) {
        int price = request.isFree() ? 0 : request.getPrice();

        CourseStatus status = request.getStatus() == null
                ? CourseStatus.DRAFT
                : request.getStatus();

        Course course = Course.builder()
                .title(request.getTitle())
                .summary(request.getSummary())
                .description(request.getDescription())
                .thumbnailUrl(request.getThumbnailUrl())
                .price(price)
                .free(request.isFree())
                .category(request.getCategory())
                .difficulty(request.getDifficulty())
                .status(status)
                .instructorId(request.getInstructorId())
                .instructorName(request.getInstructorName())
                .viewCount(0)
                .studentCount(0)
                .build();

        Course savedCourse = courseRepository.save(course);

        return savedCourse.getId();
    }

    public void updateCourse(Long courseId, CourseUpdateRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));

        course.setTitle(request.getTitle());
        course.setSummary(request.getSummary());
        course.setDescription(request.getDescription());
        course.setThumbnailUrl(request.getThumbnailUrl());
        course.setCategory(request.getCategory());
        course.setDifficulty(request.getDifficulty());
        course.setFree(request.isFree());

        if (request.isFree()) {
            course.setPrice(0);
        } else {
            course.setPrice(request.getPrice());
        }

        if (request.getStatus() != null) {
            course.setStatus(request.getStatus());
        }
    }

    public void deleteCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("강의를 찾을 수 없습니다."));

        courseRepository.delete(course);
    }
}