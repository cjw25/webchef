package com.example.fivechef.WebChef.dto;

import com.example.fivechef.WebChef.entity.Role;
import com.example.fivechef.WebChef.entity.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AdminMemberResponse {

    private final Long id;
    private final String username;
    private final String name;
    private final String email;
    private final Role role;
    private final Boolean active;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public AdminMemberResponse(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.name = user.getName();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.active = user.getActive();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
    }

    public boolean isUser() {
        return this.role == Role.USER;
    }

    public boolean isInstructor() {
        return this.role == Role.INSTRUCTOR;
    }

    public boolean isActive() {
        return Boolean.TRUE.equals(this.active);
    }
}