package com.example.fivechef.WebChef.repository;

import com.example.fivechef.WebChef.entity.InquiryAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InquiryAnswerRepository extends JpaRepository<InquiryAnswer, Long> {

    List<InquiryAnswer> findByInquiryIdOrderByCreateDateAsc(Long inquiryId);
}
