package com.example.demo.audit.repository.interfaces;

import com.example.demo.audit.entity.AuditLog;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    @Override
    @EntityGraph(attributePaths = {"performedBy"})
    List<AuditLog> findAll();

    @Override
    @EntityGraph(attributePaths = {"performedBy"})
    Optional<AuditLog> findById(Long logId);

    @EntityGraph(attributePaths = {"performedBy"})
    List<AuditLog> findByEntityTypeIgnoreCaseOrderByPerformedAtDesc(String entityType);

    @EntityGraph(attributePaths = {"performedBy"})
    List<AuditLog> findByActionTypeIgnoreCaseOrderByPerformedAtDesc(String actionType);

    boolean existsByPerformedByMemberId(Long memberId);
}
