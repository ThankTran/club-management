package com.example.demo.audit.service.impl;

import com.example.demo.audit.dto.request.AuditLogRequest;
import com.example.demo.audit.dto.response.AuditLogResponse;
import com.example.demo.audit.mapper.AuditLogMapper;
import com.example.demo.member.entity.Member;
import com.example.demo.audit.repository.AuditLogRepository;
import com.example.demo.member.repository.MemberRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@CacheConfig(cacheNames = "auditLogs")
public class AuditLogServiceImpl implements com.example.demo.audit.service.interfaces.AuditLogService {
    private final AuditLogRepository auditLogRepository;
    private final MemberRepository memberRepository;
    private final AuditLogMapper auditLogMapper;

    public AuditLogServiceImpl(
            AuditLogRepository auditLogRepository,
            MemberRepository memberRepository,
            AuditLogMapper auditLogMapper) {
        this.auditLogRepository = auditLogRepository;
        this.memberRepository = memberRepository;
        this.auditLogMapper = auditLogMapper;
    }

    @CacheEvict(allEntries = true)
    public AuditLogResponse create(AuditLogRequest request) {
        Member performedBy = null;
        if (request.getPerformedById() != null) {
            performedBy = memberRepository.findById(request.getPerformedById())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Không tìm thấy thành viên: " + request.getPerformedById()));
        }
        var entity = auditLogMapper.toEntity(request, performedBy);
        entity.setPerformedAt(LocalDateTime.now());
        return auditLogMapper.toResponse(auditLogRepository.save(entity));
    }

    @Cacheable(key = "'all'")
    public List<AuditLogResponse> getAll() {
        return auditLogRepository.findAll().stream().map(auditLogMapper::toResponse).toList();
    }

    @Cacheable(key = "'id:' + #id")
    public AuditLogResponse getById(Long id) {
        return auditLogRepository.findById(id).map(auditLogMapper::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy audit log: " + id));
    }

    @Cacheable(key = "'entity:' + #entityType")
    public List<AuditLogResponse> getByEntityType(String entityType) {
        return auditLogRepository.findByEntityTypeIgnoreCaseOrderByPerformedAtDesc(entityType).stream()
                .map(auditLogMapper::toResponse)
                .toList();
    }

    @Cacheable(key = "'action:' + #actionType")
    public List<AuditLogResponse> getByActionType(String actionType) {
        return auditLogRepository.findByActionTypeIgnoreCaseOrderByPerformedAtDesc(actionType).stream()
                .map(auditLogMapper::toResponse)
                .toList();
    }

    @Async("applicationTaskExecutor")
    public CompletableFuture<List<AuditLogResponse>> getAllAsync() {
        return CompletableFuture.completedFuture(getAll());
    }

    @Async("applicationTaskExecutor")
    public CompletableFuture<AuditLogResponse> getByIdAsync(Long id) {
        return CompletableFuture.completedFuture(getById(id));
    }
}
