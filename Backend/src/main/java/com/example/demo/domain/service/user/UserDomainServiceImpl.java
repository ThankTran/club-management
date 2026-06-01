package com.example.demo.domain.service.user;

import com.example.demo.domain.model.user.User;
import org.springframework.stereotype.Service;

@Service
public class UserDomainServiceImpl implements UserDomainService {
    private final PasswordHasher passwordHasher;

    public UserDomainServiceImpl(PasswordHasher passwordHasher) {
        this.passwordHasher = passwordHasher;
    }

    @Override
    public void validateCreateRequest(Long memberId, String password) {
        if (memberId == null) {
            throw new IllegalArgumentException("Member ID không được để trống");
        }
        validatePasswordValue(password, "Mật khẩu");
    }

    @Override
    public void validateLoginRequest(Long userId, Long memberId, String username, String password) {
        if (userId == null && memberId == null && isBlank(username)) {
            throw new IllegalArgumentException("Cần cung cấp username, userId hoặc memberId để đăng nhập");
        }
        validatePasswordValue(password, "Mật khẩu");
    }

    @Override
    public void validateChangePasswordRequest(String currentPassword, String newPassword) {
        validatePasswordValue(currentPassword, "Mật khẩu hiện tại");
        validatePasswordValue(newPassword, "Mật khẩu mới");
        if (currentPassword.equals(newPassword)) {
            throw new IllegalArgumentException("Mật khẩu mới phải khác mật khẩu hiện tại");
        }
    }

    @Override
    public void verifyLogin(User user, String rawPassword) {
        if (user == null) {
            throw new IllegalArgumentException("Tài khoản không tồn tại");
        }
        if (!passwordHasher.matches(rawPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Thông tin đăng nhập không chính xác");
        }
    }

    @Override
    public void changePassword(User user, String newPassword) {
        user.changePassword(passwordHasher.hash(newPassword));
    }

    private void validatePasswordValue(String password, String fieldName) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException(fieldName + " không được để trống");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException(fieldName + " phải có ít nhất 6 ký tự");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
