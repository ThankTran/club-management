package com.example.demo.user.service.interfaces;

import com.example.demo.user.dto.request.ChangePasswordRequest;
import com.example.demo.user.dto.request.CreateUserRequest;
import com.example.demo.auth.dto.request.LoginRequest;
import com.example.demo.user.dto.response.UserPasswordResponse;
import com.example.demo.user.dto.response.UserResponse;
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
