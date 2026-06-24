package com.example.fivechef.WebChef.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreateRequest {

    private String username;

    private String password;

    private String passwordCheck;

    private String name;

    private String email;
}