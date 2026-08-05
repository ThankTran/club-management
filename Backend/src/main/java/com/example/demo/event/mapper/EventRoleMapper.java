package com.example.demo.event.mapper;

import com.example.demo.event.dto.request.EventRoleRequest;
import com.example.demo.event.dto.response.EventRoleResponse;
import com.example.demo.event.entity.EventRole;
import com.example.demo.shared.config.GlobalMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = GlobalMapperConfig.class)
public interface EventRoleMapper {
    EventRole toEntity(EventRoleRequest request);
    EventRoleResponse toResponse(EventRole entity);
}

