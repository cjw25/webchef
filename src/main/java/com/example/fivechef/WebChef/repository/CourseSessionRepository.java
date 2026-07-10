package com.example.fivechef.WebChef.repository;

import com.example.fivechef.WebChef.entity.CourseSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseSessionRepository extends JpaRepository<CourseSession, Long> {

    List<CourseSession> findByCourseIdOrderBySortOrderAsc(Long courseId);
}
