package com.example.demo.role.mapper;

import com.example.demo.role.dto.request.RoleRequest;
import com.example.demo.role.dto.response.RoleResponse;
import com.example.demo.role.entity.Role;
import com.example.demo.shared.config.GlobalMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = GlobalMapperConfig.class)
public interface RoleMapper {
    Role toEntity(RoleRequest request);
    RoleResponse toResponse(Role entity);
}

