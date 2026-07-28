package com.example.demo.event.dto.response;

import com.example.demo.shared.enums.ApprovalStatusEnum;
import com.example.demo.shared.enums.EventStatusEnum;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventResponse {
    private String eventId;
    private String eventName;
    private String location;
    private LocalDate eventDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal estimatedCost;
    private Integer capacity;
    private String organizer;
    private String tag;
    private Long attendance;
    private EventStatusEnum status;
    private ApprovalStatusEnum reqStatus;
    private String description;
    private Long evaluatedById;
    private LocalDateTime evaluationDate;
    private String evaluationContent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
