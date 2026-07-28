package com.example.demo.auth.service.interfaces;

public interface AuthTokenService {
    String createToken(Long userId);

    Long parseUserId(String authorizationHeader);
}
