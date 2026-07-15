package com.example.fivechef.WebChef.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "chat_usages")
public class ChatUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 챗봇 사용 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDate usageDate;

    private Integer count = 0;

    @PrePersist
    public void prePersist() {
        if (this.usageDate == null) {
            this.usageDate = LocalDate.now();
        }

        if (this.count == null) {
            this.count = 0;
        }
    }
}