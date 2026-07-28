package com.example.demo.role.mapper;

import com.example.demo.role.dto.request.RoleRequest;
import com.example.demo.role.dto.response.RoleResponse;
import com.example.demo.role.entity.Role;
import org.springframework.stereotype.Component;

@Component
public class RoleMapper {

    public Role toEntity(RoleRequest request) {
        return Role.builder()
                .roleName(request.getRoleName())
                .priority(request.getPriority())
                .build();
    }

    public RoleResponse toResponse(Role entity) {
        return RoleResponse.builder()
                .roleId(entity.getRoleId())
                .roleName(entity.getRoleName())
                .priority(entity.getPriority())
                .build();
    }
}
