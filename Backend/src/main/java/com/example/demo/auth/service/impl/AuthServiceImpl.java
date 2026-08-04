package com.example.demo.auth.service.impl;

import com.example.demo.user.dto.request.CreateUserRequest;
import com.example.demo.auth.dto.request.LoginRequest;
import com.example.demo.auth.dto.response.AuthResponse;
import com.example.demo.user.dto.response.UserResponse;
import com.example.demo.auth.service.interfaces.AuthService;
import com.example.demo.auth.service.interfaces.AuthTokenService;
import com.example.demo.user.service.interfaces.LoginSessionService;
import com.example.demo.user.service.interfaces.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthServiceImpl implements AuthService {
    UserService userService;
    AuthTokenService authTokenService;
    LoginSessionService loginSessionService;

    @Override
    public AuthResponse login(LoginRequest request, HttpServletRequest servletRequest) {
        UserResponse user = userService.login(request);
        loginSessionService.recordLogin(user.getUserId(), servletRequest);
        return AuthResponse.builder()
                .user(user)
                .token(authTokenService.createToken(user.getUserId()))
                .build();
    }

    @Override
    public AuthResponse register(CreateUserRequest request) {
        UserResponse user = userService.createUser(request);
        return AuthResponse.builder()
                .user(user)
                .token(authTokenService.createToken(user.getUserId()))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String authorizationHeader) {
        Long userId = authTokenService.parseUserId(authorizationHeader);
        return userService.getUserById(userId);
    }
}
