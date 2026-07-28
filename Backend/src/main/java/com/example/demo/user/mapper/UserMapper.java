package com.example.demo.user.mapper;

import com.example.demo.user.dto.response.UserPasswordResponse;
import com.example.demo.user.dto.response.UserResponse;
import com.example.demo.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserResponse toResponse(User entity) {
        var member = entity.getMember();
        var department = member.getDepartment();
        var role = member.getRole();

        return UserResponse.builder()
                .userId(entity.getUserId())
                .memberId(member.getMemberId())
                .studentId(member.getStudentId())
                .fullName(member.getFullName())
                .email(member.getEmail())
                .departmentId(department == null ? null : department.getDepartmentId())
                .departmentName(department == null ? null : department.getDepartmentName())
                .roleId(role == null ? null : role.getRoleId())
                .roleName(role == null ? null : role.getRoleName())
                .rolePriority(role == null ? null : role.getPriority())
                .reqStatus(member.getReqStatus() == null ? null : member.getReqStatus().name())
                .graduatedStatus(member.getGraduatedStatus() == null ? null : member.getGraduatedStatus().name())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public UserPasswordResponse toPasswordResponse(User entity) {
        return UserPasswordResponse.builder()
                .userId(entity.getUserId())
                .memberId(entity.getMember().getMemberId())
                .passwordHash(entity.getPasswordHash())
                .build();
    }
}
