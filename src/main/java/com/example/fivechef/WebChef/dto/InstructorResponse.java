package com.example.fivechef.WebChef.dto;

import com.example.fivechef.WebChef.entity.Role;
import com.example.fivechef.WebChef.entity.User;
import lombok.Getter;

@Getter
public class InstructorResponse {

    private final Long id;

    private final String username;

    private final String name;

    private final String email;

    private final Role role;

    public InstructorResponse(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.name = user.getName();
        this.email = user.getEmail();
        this.role = user.getRole();
    }
}