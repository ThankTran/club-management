package com.example.demo.finance.repository;

import com.example.demo.shared.enums.TransactionType;
import com.example.demo.shared.enums.TransactionStatus;
import com.example.demo.finance.entity.Transaction;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TransactionRepository extends JpaRepository<Transaction, String> {
    @EntityGraph(attributePaths = {"event", "member", "createdBy", "approvedBy"})
    Optional<Transaction> findById(String transactionId);

    @EntityGraph(attributePaths = {"event", "member", "createdBy", "approvedBy"})
    @Query("""
            SELECT t
            FROM Transaction t
            WHERE t.deletedAt IS NULL
            ORDER BY COALESCE(t.transactionDate, t.createdAt) DESC
            """)
    List<Transaction> findActive();

    @EntityGraph(attributePaths = {"event", "member", "createdBy", "approvedBy"})
    @Query(value = """
            SELECT t
            FROM Transaction t
            WHERE t.deletedAt IS NULL
              AND (:type IS NULL OR t.type = :type)
              AND (:from IS NULL OR COALESCE(t.transactionDate, t.createdAt) >= :from)
              AND (:to IS NULL OR COALESCE(t.transactionDate, t.createdAt) <= :to)
            ORDER BY COALESCE(t.transactionDate, t.createdAt) DESC
            """,
            countQuery = """
            SELECT COUNT(t)
            FROM Transaction t
            WHERE t.deletedAt IS NULL
              AND (:type IS NULL OR t.type = :type)
              AND (:from IS NULL OR COALESCE(t.transactionDate, t.createdAt) >= :from)
              AND (:to IS NULL OR COALESCE(t.transactionDate, t.createdAt) <= :to)
            """)
    Page<Transaction> findActivePage(
            @Param("type") TransactionType type,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable);

    @EntityGraph(attributePaths = {"event", "member", "createdBy", "approvedBy"})
    @Query("""
            SELECT t
            FROM Transaction t
            WHERE t.deletedAt IS NULL
              AND t.type = :type
            ORDER BY COALESCE(t.transactionDate, t.createdAt) DESC
            """)
    List<Transaction> findActiveByType(@Param("type") TransactionType type);

    @EntityGraph(attributePaths = {"event", "member", "createdBy", "approvedBy"})
    @Query("""
            SELECT t
            FROM Transaction t
            WHERE t.deletedAt IS NULL
              AND t.event.eventId = :eventId
            ORDER BY COALESCE(t.transactionDate, t.createdAt) DESC
            """)
    List<Transaction> findActiveByEventId(@Param("eventId") String eventId);

    @EntityGraph(attributePaths = {"event", "member", "createdBy", "approvedBy"})
    @Query("""
            SELECT t
            FROM Transaction t
            WHERE t.deletedAt IS NULL
              AND t.member.memberId = :memberId
              AND t.type = :type
            ORDER BY COALESCE(t.transactionDate, t.createdAt) DESC
            """)
    List<Transaction> findActiveByMemberIdAndType(
            @Param("memberId") Long memberId, @Param("type") TransactionType type);

    @EntityGraph(attributePaths = {"event", "member", "createdBy", "approvedBy"})
    @Query("""
            SELECT t
            FROM Transaction t
            WHERE t.deletedAt IS NULL
              AND t.member.memberId = :memberId
              AND t.event.eventId = :eventId
              AND t.type = :type
              AND t.status = :status
            ORDER BY COALESCE(t.transactionDate, t.createdAt) DESC
            """)
    List<Transaction> findActiveByMemberIdAndEventIdAndTypeAndStatus(
            @Param("memberId") Long memberId,
            @Param("eventId") String eventId,
            @Param("type") TransactionType type,
            @Param("status") TransactionStatus status);

    @EntityGraph(attributePaths = {"event", "member", "createdBy", "approvedBy"})
    @Query("""
            SELECT t
            FROM Transaction t
            WHERE t.deletedAt IS NULL
              AND t.type = :type
              AND t.event.eventId = :eventId
            ORDER BY COALESCE(t.transactionDate, t.createdAt) DESC
            """)
    List<Transaction> findActiveByTypeAndEventId(
            @Param("type") TransactionType type, @Param("eventId") String eventId);

    @EntityGraph(attributePaths = {"event", "member", "createdBy", "approvedBy"})
    @Query("""
            SELECT t
            FROM Transaction t
            WHERE t.deletedAt IS NULL
              AND COALESCE(t.transactionDate, t.createdAt) BETWEEN :from AND :to
            ORDER BY COALESCE(t.transactionDate, t.createdAt) DESC
            """)
    List<Transaction> findActiveByCreatedAtBetween(
            @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @EntityGraph(attributePaths = {"event", "member", "createdBy", "approvedBy"})
    @Query("""
            SELECT t
            FROM Transaction t
            WHERE t.deletedAt IS NULL
              AND t.type = :type
              AND COALESCE(t.transactionDate, t.createdAt) BETWEEN :from AND :to
            ORDER BY COALESCE(t.transactionDate, t.createdAt) DESC
            """)
    List<Transaction> findActiveByTypeAndCreatedAtBetween(
            @Param("type") TransactionType type,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    @Query("""
            SELECT COALESCE(SUM(t.amount), 0)
            FROM Transaction t
            WHERE t.deletedAt IS NULL
              AND t.type = :type
              AND t.status IN (com.example.demo.shared.enums.TransactionStatus.COMPLETED, com.example.demo.shared.enums.TransactionStatus.APPROVED)
              AND COALESCE(t.transactionDate, t.createdAt) BETWEEN :from AND :to
            """)
    java.math.BigDecimal sumCompletedAmountByTypeAndDateBetween(
            @Param("type") TransactionType type,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    @Query("""
            SELECT COALESCE(SUM(t.amount), 0)
            FROM Transaction t
            WHERE t.deletedAt IS NULL
              AND t.type = :type
              AND t.status IN (com.example.demo.shared.enums.TransactionStatus.COMPLETED, com.example.demo.shared.enums.TransactionStatus.APPROVED)
              AND t.event.eventId = :eventId
            """)
    java.math.BigDecimal sumCompletedAmountByTypeAndEventId(
            @Param("type") TransactionType type,
            @Param("eventId") String eventId);

    @EntityGraph(attributePaths = {"member", "member.role"})
    @Query("""
            SELECT t
            FROM Transaction t
            WHERE t.deletedAt IS NULL
              AND t.type = :type
              AND t.description = :description
            ORDER BY COALESCE(t.transactionDate, t.createdAt) DESC
            """)
    List<Transaction> findActiveByTypeAndDescription(
            @Param("type") TransactionType type,
            @Param("description") String description);

    boolean existsByEventEventId(String eventId);

    boolean existsByMemberMemberId(Long memberId);

    boolean existsByCreatedByMemberId(Long memberId);

    boolean existsByApprovedByMemberId(Long memberId);
}
