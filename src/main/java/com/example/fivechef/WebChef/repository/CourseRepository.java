package com.example.fivechef.WebChef.repository;

import com.example.fivechef.WebChef.entity.Course;
import com.example.fivechef.WebChef.entity.Course.CourseCategory;
import com.example.fivechef.WebChef.entity.Course.CourseDifficulty;
import com.example.fivechef.WebChef.entity.Course.CourseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByStatusOrderByCreatedAtDesc(CourseStatus status);

    List<Course> findByCategoryAndStatusOrderByCreatedAtDesc(
            CourseCategory category,
            CourseStatus status
    );

    List<Course> findByDifficultyAndStatusOrderByCreatedAtDesc(
            CourseDifficulty difficulty,
            CourseStatus status
    );

    List<Course> findTop4ByStatusOrderByStudentCountDesc(CourseStatus status);

    @Query("""
            SELECT c
            FROM Course c
            WHERE c.status = :status
              AND (
                    c.title LIKE %:keyword%
                    OR c.summary LIKE %:keyword%
                    OR c.description LIKE %:keyword%
                  )
            ORDER BY c.createdAt DESC
            """)
    List<Course> searchOpenCourses(
            @Param("keyword") String keyword,
            @Param("status") CourseStatus status
    );
}