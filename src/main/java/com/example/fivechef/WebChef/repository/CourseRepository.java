package com.example.fivechef.WebChef.repository;

import com.example.fivechef.WebChef.entity.Course;
import com.example.fivechef.WebChef.entity.CourseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {

    Page<Course> findByTitleContainingOrDescriptionContaining(
            String titleKeyword,
            String descriptionKeyword,
            Pageable pageable
    );

    Page<Course> findByStatus(
            CourseStatus status,
            Pageable pageable
    );
}