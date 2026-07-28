package com.example.demo.user.dto.request;

import lombok.Data;

@Data
public class CreateUserRequest {
    private Long memberId;
    private String password;
}
