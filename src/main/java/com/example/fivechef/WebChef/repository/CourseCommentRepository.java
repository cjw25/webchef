package com.example.fivechef.WebChef.repository;

import com.example.fivechef.WebChef.entity.CourseComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseCommentRepository extends JpaRepository<CourseComment, Long> {

    Page<CourseComment> findByCourseIdOrderByCreateDateDesc(Long courseId, Pageable pageable);
}