package com.example.demo.event.entity;

import com.example.demo.shared.enums.ApprovalStatusEnum;
import com.example.demo.shared.enums.EventStatusEnum;
import com.example.demo.member.entity.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
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
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@DynamicUpdate
@SQLRestriction("deleted_at IS NULL")
public class Event {
    @Id
    @Column(name = "event_id")
    private String eventId;

    @Column(name = "event_name", nullable = false)
    private String eventName;

    @Column(name = "location")
    private String location;

    @Column(name = "event_date")
    private LocalDate eventDate;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "estimated_cost")
    private BigDecimal estimatedCost;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "organizer", length = 255)
    private String organizer;

    @Column(name = "tag", length = 50)
    private String tag;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    @Builder.Default
    private EventStatusEnum status = EventStatusEnum.NotStarted;

    @Enumerated(EnumType.STRING)
    @Column(name = "req_status", length = 50)
    @Builder.Default
    private ApprovalStatusEnum reqStatus = ApprovalStatusEnum.PENDING;

    @Column(name = "description", columnDefinition = "nvarchar(max)")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluated_by")
    private Member evaluatedBy;

    @Column(name = "evaluation_date")
    private LocalDateTime evaluationDate;

    @Column(name = "evaluation_content", columnDefinition = "nvarchar(max)")
    private String evaluationContent;


    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
