package com.example.fivechef.WebChef.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
public class User {

        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Id
        private Long id;

        @Column(unique = true, nullable = false, updatable = false)
        private String username;

        private String password;

        @Column(unique = true)
        private String email;

    }

