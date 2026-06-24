package com.example.fivechef.WebChef.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FindPasswordRequest {

    private String username;

    private String email;
}