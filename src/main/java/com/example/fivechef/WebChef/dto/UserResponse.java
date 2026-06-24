package com.example.fivechef.WebChef.dto;

import com.example.fivechef.WebChef.entity.Role;
import com.example.fivechef.WebChef.entity.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserResponse {

    private final Long id;
    private final String username;
    private final String name;
    private final String email;
    private final Role role;
    private final Boolean active;
    private final LocalDateTime createdAt;

    public UserResponse(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.name = user.getName();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.active = user.getActive();
        this.createdAt = user.getCreatedAt();
    }
}