package com.example.demo.audit.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogResponse {
    private Long logId;
    private String entityType;
    private String entityId;
    private String actionType;
    private String oldValue;
    private String newValue;
    private Long performedById;
    private LocalDateTime performedAt;
}
