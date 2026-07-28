package com.example.demo.user.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long userId;
    private Long memberId;
    private String studentId;
    private String fullName;
    private String email;
    private Long departmentId;
    private String departmentName;
    private Long roleId;
    private String roleName;
    private Integer rolePriority;
    private String reqStatus;
    private String graduatedStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
