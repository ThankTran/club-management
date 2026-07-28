package com.example.demo.auth.service.impl;

import com.example.demo.auth.service.interfaces.AuthTokenService;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.stereotype.Service;

@Service
public class AuthTokenServiceImpl implements AuthTokenService {
    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String TOKEN_USER_PREFIX = "user:";

    @Override
    public String createToken(Long userId) {
        String payload = TOKEN_USER_PREFIX + userId;
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Long parseUserId(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(TOKEN_PREFIX)) {
            throw new IllegalArgumentException("Missing authorization token");
        }

        String token = authorizationHeader.substring(TOKEN_PREFIX.length());
        String payload;
        try {
            payload = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid authorization token");
        }

        if (!payload.startsWith(TOKEN_USER_PREFIX)) {
            throw new IllegalArgumentException("Invalid authorization token");
        }

        try {
            return Long.valueOf(payload.substring(TOKEN_USER_PREFIX.length()));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid authorization token");
        }
    }
}
