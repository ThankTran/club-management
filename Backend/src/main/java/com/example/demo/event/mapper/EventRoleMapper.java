package com.example.demo.event.mapper;

import com.example.demo.event.dto.request.EventRoleRequest;
import com.example.demo.event.dto.response.EventRoleResponse;
import com.example.demo.event.entity.EventRole;
import org.springframework.stereotype.Component;

@Component
public class EventRoleMapper {

    public EventRole toEntity(EventRoleRequest request) {
        return EventRole.builder()
                .roleId(request.getRoleId())
                .roleName(request.getRoleName())
                .build();
    }

    public EventRoleResponse toResponse(EventRole entity) {
        return EventRoleResponse.builder()
                .roleId(entity.getRoleId())
                .roleName(entity.getRoleName())
                .build();
    }
}
