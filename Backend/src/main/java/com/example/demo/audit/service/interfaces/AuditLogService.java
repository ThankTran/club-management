package com.example.demo.audit.service.interfaces;

import com.example.demo.audit.dto.request.AuditLogRequest;
import com.example.demo.audit.dto.response.AuditLogResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface AuditLogService {
    AuditLogResponse create(AuditLogRequest request);

    List<AuditLogResponse> getAll();

    AuditLogResponse getById(Long id);

    List<AuditLogResponse> getByEntityType(String entityType);

    List<AuditLogResponse> getByActionType(String actionType);

    CompletableFuture<List<AuditLogResponse>> getAllAsync();

    CompletableFuture<AuditLogResponse> getByIdAsync(Long id);
}
