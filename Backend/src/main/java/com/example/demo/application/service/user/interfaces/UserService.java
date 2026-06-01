package com.example.demo.application.service.user.interfaces;

import com.example.demo.application.dto.request.user.ChangePasswordRequest;
import com.example.demo.application.dto.request.user.CreateUserRequest;
import com.example.demo.application.dto.request.user.LoginRequest;
import com.example.demo.application.dto.response.user.UserPasswordResponse;
import com.example.demo.application.dto.response.user.UserResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface UserService {
    UserResponse createUser(CreateUserRequest request);

    UserResponse login(LoginRequest request);

    UserResponse getUserById(Long userId);

    UserResponse getUserByMemberId(Long memberId);

    List<UserResponse> getAllUsers();

    UserResponse changePassword(Long userId, ChangePasswordRequest request);

    UserResponse updatePasswordForAdmin(Long userId, String newPassword);

    UserPasswordResponse getPasswordHashForAdmin(Long userId);

    void deleteUser(Long userId, Long currentUserId);

    CompletableFuture<UserResponse> getUserByIdAsync(Long userId);

    CompletableFuture<UserResponse> getUserByMemberIdAsync(Long memberId);
}
