package com.example.fivechef.WebChef.repository;

import com.example.fivechef.WebChef.entity.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {
}