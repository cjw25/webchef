package com.example.fivechef.WebChef.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminMemberUpdateRequest {

    private String name;

    private String email;

    private String password;

    private String passwordCheck;
}