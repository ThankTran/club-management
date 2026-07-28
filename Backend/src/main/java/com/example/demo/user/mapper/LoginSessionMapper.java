package com.example.demo.user.mapper;

import com.example.demo.user.dto.response.LoginSessionResponse;
import com.example.demo.user.entity.LoginSession;
import org.springframework.stereotype.Component;

@Component
public class LoginSessionMapper {
    public LoginSessionResponse toResponse(LoginSession session) {
        return LoginSessionResponse.builder()
                .sessionId(session.getSessionId())
                .userId(session.getUser().getUserId())
                .loginAt(session.getLoginAt())
                .ipAddress(session.getIpAddress())
                .userAgent(session.getUserAgent())
                .deviceLabel(session.getDeviceLabel())
                .build();
    }
}
