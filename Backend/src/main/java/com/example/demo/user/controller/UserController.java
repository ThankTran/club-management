package com.example.demo.user.controller;

import com.example.demo.user.dto.request.AdminUpdateUserRequest;
import com.example.demo.user.dto.request.ChangePasswordRequest;
import com.example.demo.user.dto.request.CreateUserRequest;
import com.example.demo.user.dto.response.UserResponse;
import com.example.demo.user.service.interfaces.LoginSessionService;
import com.example.demo.user.service.interfaces.UserService;
import com.example.demo.shared.security.AccessControlInterceptor;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final LoginSessionService loginSessionService;

    public UserController(UserService userService, LoginSessionService loginSessionService) {
        this.userService = userService;
        this.loginSessionService = loginSessionService;
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request) {
        try {
            UserResponse response = userService.createUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        try {
            List<UserResponse> response = userService.getAllUsers();
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(userService.getUserById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/by-member/{memberId}")
    public ResponseEntity<?> getUserByMemberId(@PathVariable Long memberId) {
        try {
            return ResponseEntity.ok(userService.getUserByMemberId(memberId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<?> changeOwnPassword(@PathVariable Long id,
                                               @RequestBody ChangePasswordRequest request) {
        try {
            return ResponseEntity.ok(userService.changePassword(id, request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/admin")
    public ResponseEntity<?> updateUserForAdmin(@PathVariable Long id,
                                                @RequestBody AdminUpdateUserRequest request) {
        try {
            return ResponseEntity.ok(userService.updatePasswordForAdmin(id, request.getNewPassword()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}/password-hash")
    public ResponseEntity<?> getPasswordHashForAdmin(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(userService.getPasswordHashForAdmin(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/{id}/sessions")
    public ResponseEntity<?> getSessions(
            @PathVariable Long id,
            @RequestAttribute(value = AccessControlInterceptor.CURRENT_USER_ATTRIBUTE, required = false) UserResponse currentUser,
            @RequestAttribute(value = AccessControlInterceptor.CURRENT_USER_IS_MANAGER_ATTRIBUTE, required = false) Boolean currentUserIsManager) {
        if (!Boolean.TRUE.equals(currentUserIsManager)
                && (currentUser == null || !id.equals(currentUser.getUserId()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Bạn không có quyền xem phiên đăng nhập này");
        }
        return ResponseEntity.ok(loginSessionService.getSessionsByUser(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(
            @PathVariable Long id,
            @RequestAttribute(value = AccessControlInterceptor.CURRENT_USER_ATTRIBUTE, required = false) UserResponse currentUser) {
        if (currentUser != null && id != null && id.equals(currentUser.getUserId())) {
            return ResponseEntity.badRequest().body("Không thể xóa tài khoản đang đăng nhập.");
        }
        try {
            userService.deleteUser(id, currentUser == null ? null : currentUser.getUserId());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
