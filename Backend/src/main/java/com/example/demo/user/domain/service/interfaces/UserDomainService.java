package com.example.demo.user.domain.service.interfaces;

import com.example.demo.user.entity.User;

public interface UserDomainService {
    void validateCreateRequest(Long memberId, String password);

    void validateLoginRequest(Long userId, Long memberId, String username, String password);

    void validateChangePasswordRequest(String currentPassword, String newPassword);

    void verifyLogin(User user, String rawPassword);

    void changePassword(User user, String newPassword);
}
