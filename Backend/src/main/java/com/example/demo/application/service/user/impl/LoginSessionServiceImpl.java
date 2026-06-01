package com.example.demo.application.service.user.impl;

import com.example.demo.application.dto.response.user.LoginSessionResponse;
import com.example.demo.application.mapper.user.LoginSessionMapper;
import com.example.demo.application.service.user.interfaces.LoginSessionService;
import com.example.demo.domain.model.user.LoginSession;
import com.example.demo.domain.repository.user.LoginSessionRepository;
import com.example.demo.domain.repository.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LoginSessionServiceImpl implements LoginSessionService {
    private final LoginSessionRepository loginSessionRepository;
    private final UserRepository userRepository;
    private final LoginSessionMapper loginSessionMapper;

    public LoginSessionServiceImpl(
            LoginSessionRepository loginSessionRepository,
            UserRepository userRepository,
            LoginSessionMapper loginSessionMapper) {
        this.loginSessionRepository = loginSessionRepository;
        this.userRepository = userRepository;
        this.loginSessionMapper = loginSessionMapper;
    }

    @Override
    public void recordLogin(Long userId, HttpServletRequest request) {
        if (userId == null) {
            return;
        }
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng: " + userId));
        String userAgent = request == null ? "" : safeHeader(request, "User-Agent");
        LoginSession session = LoginSession.builder()
                .user(user)
                .loginAt(LocalDateTime.now())
                .ipAddress(resolveIpAddress(request))
                .userAgent(userAgent)
                .deviceLabel(resolveDeviceLabel(userAgent))
                .build();
        loginSessionRepository.save(session);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LoginSessionResponse> getSessionsByUser(Long userId) {
        return loginSessionRepository.findTop20ByUserUserIdOrderByLoginAtDesc(userId).stream()
                .map(loginSessionMapper::toResponse)
                .toList();
    }

    private String resolveIpAddress(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        String forwardedFor = safeHeader(request, "X-Forwarded-For");
        if (!forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String safeHeader(HttpServletRequest request, String name) {
        String value = request.getHeader(name);
        return value == null ? "" : value.trim();
    }

    private String resolveDeviceLabel(String userAgent) {
        String browser = "Trình duyệt";
        if (userAgent.contains("Edg/")) {
            browser = "Edge";
        } else if (userAgent.contains("Chrome/")) {
            browser = "Chrome";
        } else if (userAgent.contains("Firefox/")) {
            browser = "Firefox";
        } else if (userAgent.contains("Safari/")) {
            browser = "Safari";
        }

        String platform = "Thiết bị không xác định";
        if (userAgent.contains("Windows")) {
            platform = "Windows";
        } else if (userAgent.contains("Android")) {
            platform = "Android";
        } else if (userAgent.contains("iPhone") || userAgent.contains("iPad")) {
            platform = "iOS";
        } else if (userAgent.contains("Mac OS X")) {
            platform = "macOS";
        } else if (userAgent.contains("Linux")) {
            platform = "Linux";
        }

        return browser + " tren " + platform;
    }
}
