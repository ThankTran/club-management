package com.example.demo.audit.mapper;

import com.example.demo.audit.dto.request.AuditLogRequest;
import com.example.demo.audit.dto.response.AuditLogResponse;
import com.example.demo.audit.entity.AuditLog;
import com.example.demo.member.entity.Member;
import com.example.demo.shared.config.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfig.class)
public interface AuditLogMapper {

    @Mapping(target = "performedBy", source = "performedBy")
    AuditLog toEntity(AuditLogRequest request, Member performedBy);

    @Mapping(source = "performedBy.memberId", target = "performedById")
    AuditLogResponse toResponse(AuditLog entity);
}

