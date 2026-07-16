package com.example.fivechef.WebChef.repository;

import com.example.fivechef.WebChef.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    Optional<Quiz> findByCourseId(Long courseId);
}