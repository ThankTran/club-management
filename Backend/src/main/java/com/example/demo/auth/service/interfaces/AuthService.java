package com.example.demo.auth.service.interfaces;

import com.example.demo.user.dto.request.CreateUserRequest;
import com.example.demo.auth.dto.request.LoginRequest;
import com.example.demo.auth.dto.response.AuthResponse;
import com.example.demo.user.dto.response.UserResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    AuthResponse login(LoginRequest request, HttpServletRequest servletRequest);

    AuthResponse register(CreateUserRequest request);

    UserResponse getCurrentUser(String authorizationHeader);
}
