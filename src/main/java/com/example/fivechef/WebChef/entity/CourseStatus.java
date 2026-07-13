package com.example.fivechef.WebChef.entity;

public enum CourseStatus {
    PENDING,        // 강사 등록 후 승인 대기
    OPEN,           // 관리자 승인 완료, USER에게 공개
    REJECTED,       // 관리자 반려
    UPDATE_PENDING, // 강사가 수정 후 재승인 대기
    CLOSED          // 비공개 / 종료
}