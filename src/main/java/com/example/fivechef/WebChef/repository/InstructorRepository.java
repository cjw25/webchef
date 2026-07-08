package com.example.fivechef.WebChef.repository;

import com.example.fivechef.WebChef.entity.Instructor;
import com.example.fivechef.WebChef.entity.InstructorStatus;
import com.example.fivechef.WebChef.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InstructorRepository extends JpaRepository<Instructor, Long> {

    Page<Instructor> findByStatus(
            InstructorStatus status,
            Pageable pageable
    );

    Optional<Instructor> findByUser(User user);

    boolean existsByUserAndStatus(
            User user,
            InstructorStatus status
    );

    long countByStatus(InstructorStatus status);
}