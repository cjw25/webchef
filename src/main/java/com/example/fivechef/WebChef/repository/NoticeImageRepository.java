package com.example.fivechef.WebChef.repository;

import com.example.fivechef.WebChef.entity.NoticeImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoticeImageRepository extends JpaRepository<NoticeImage, Long> {

    List<NoticeImage> findByNoticeIdOrderByIdAsc(Long noticeId);

    void deleteByNoticeId(Long noticeId);
}