package com.example.demo.audit.controller;

import com.example.demo.audit.dto.request.AuditLogRequest;
import com.example.demo.audit.dto.response.AuditLogResponse;
import com.example.demo.audit.service.interfaces.AuditLogService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody AuditLogRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(auditLogService.create(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<AuditLogResponse>> getAll() {
        return ResponseEntity.ok(auditLogService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(auditLogService.getById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/by-entity-type")
    public ResponseEntity<List<AuditLogResponse>> byEntityType(@RequestParam String type) {
        return ResponseEntity.ok(auditLogService.getByEntityType(type));
    }

    @GetMapping("/by-action-type")
    public ResponseEntity<List<AuditLogResponse>> byActionType(@RequestParam String action) {
        return ResponseEntity.ok(auditLogService.getByActionType(action));
    }
}
