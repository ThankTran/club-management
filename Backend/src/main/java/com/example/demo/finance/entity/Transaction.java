package com.example.demo.finance.entity;

import com.example.demo.shared.enums.TransactionStatus;
import com.example.demo.shared.enums.TransactionType;
import com.example.demo.event.entity.Event;
import com.example.demo.member.entity.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.SQLRestriction;


@Entity
@Table(
        name = "transactions",
        indexes = {
                @jakarta.persistence.Index(name = "idx_transactions_type_deleted", columnList = "type, deleted_at"),
                @jakarta.persistence.Index(name = "idx_transactions_member_type_deleted", columnList = "member_id, type, deleted_at"),
                @jakarta.persistence.Index(name = "idx_transactions_event_type_deleted", columnList = "event_id, type, deleted_at"),
                @jakarta.persistence.Index(name = "idx_transactions_dates_deleted", columnList = "transaction_date, created_at, deleted_at")
        })
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@DynamicUpdate
@SQLRestriction("deleted_at IS NULL")
public class Transaction {
    @Id
    @Column(name = "transaction_id", length = 50)
    private String transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "counterparty_name", length = 255)
    private String counterpartyName;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private TransactionType type;

    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "description", columnDefinition = "nvarchar(max)")
    private String description;

    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", length = 50)
    private TransactionStatus status  = TransactionStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private Member createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private Member approvedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    public void prePersist() {
        if (transactionDate == null) {
            transactionDate = LocalDateTime.now();
        }
    }
}
