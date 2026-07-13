package com.example.fivechef.WebChef.repository;

import com.example.fivechef.WebChef.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    Optional<QuizAttempt> findTopByQuizIdAndStudentIdOrderByCreateDateDesc(Long quizId, Long studentId);
}
