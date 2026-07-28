package com.example.demo.audit.mapper;

import com.example.demo.audit.dto.request.AuditLogRequest;
import com.example.demo.audit.dto.response.AuditLogResponse;
import com.example.demo.audit.entity.AuditLog;
import com.example.demo.member.entity.Member;
import org.springframework.stereotype.Component;

@Component
public class AuditLogMapper {

    public AuditLog toEntity(AuditLogRequest request, Member performedBy) {
        return AuditLog.builder()
                .entityType(request.getEntityType())
                .entityId(request.getEntityId())
                .actionType(request.getActionType())
                .oldValue(request.getOldValue())
                .newValue(request.getNewValue())
                .performedBy(performedBy)
                .build();
    }

    public AuditLogResponse toResponse(AuditLog entity) {
        Long performedById = entity.getPerformedBy() == null ? null : entity.getPerformedBy().getMemberId();
        return AuditLogResponse.builder()
                .logId(entity.getLogId())
                .entityType(entity.getEntityType())
                .entityId(entity.getEntityId())
                .actionType(entity.getActionType())
                .oldValue(entity.getOldValue())
                .newValue(entity.getNewValue())
                .performedById(performedById)
                .performedAt(entity.getPerformedAt())
                .build();
    }
}
