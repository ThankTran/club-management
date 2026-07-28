package com.example.demo.audit.dto.request;

import lombok.Data;

@Data
public class AuditLogRequest {
    private String entityType;
    private String entityId;
    private String actionType;
    private String oldValue;
    private String newValue;
    private Long performedById;
}
