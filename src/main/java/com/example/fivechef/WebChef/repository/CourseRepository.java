package com.example.fivechef.WebChef.repository;

import com.example.fivechef.WebChef.entity.Course;
import com.example.fivechef.WebChef.entity.CourseStatus;
import com.example.fivechef.WebChef.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long>, JpaSpecificationExecutor<Course> {

    Page<Course> findByTitleContainingOrDescriptionContaining(
            String titleKeyword,
            String descriptionKeyword,
            Pageable pageable
    );

    Page<Course> findByStatus(
            CourseStatus status,
            Pageable pageable
    );

    List<Course> findByInstructorOrderByIdDesc(User instructor);

    List<Course> findByStatusInOrderByIdDesc(Collection<CourseStatus> statuses);
}