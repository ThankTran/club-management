package com.example.demo.user.service.interfaces;

import com.example.demo.user.dto.response.LoginSessionResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

public interface LoginSessionService {
    void recordLogin(Long userId, HttpServletRequest request);

    List<LoginSessionResponse> getSessionsByUser(Long userId);
}
