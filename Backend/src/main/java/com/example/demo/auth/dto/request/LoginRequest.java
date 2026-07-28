package com.example.demo.auth.dto.request;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private Long userId;
    private Long memberId;
    private String password;
}
