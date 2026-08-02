package com.example.demo.event.dto.request;

import com.example.demo.shared.enums.ApprovalStatusEnum;
import com.example.demo.shared.enums.EventStatusEnum;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class EventRequest {
    @NotBlank(message = "Event ID must not be empty")
    private String eventId;

    @NotBlank(message = "Event name must not be empty")
    private String eventName;

    private String location;

    @NotNull(message = "Event date must not be empty")
    @FutureOrPresent(message = "Event date must be today or later")
    private LocalDate eventDate;

    @NotNull(message = "Start time must not be empty")
    private LocalDateTime startTime;

    @NotNull(message = "End time must not be empty")
    private LocalDateTime endTime;

    @Min(value = 0, message = "Estimated cost must not be negative")
    private BigDecimal estimatedCost;

    @Min(value = 0, message = "Capacity must not be negative")
    private Integer capacity;

    private String organizer;
    private String tag;
    private EventStatusEnum status;
    private ApprovalStatusEnum reqStatus;
    private String description;
    private Long evaluatedById;
    private LocalDateTime evaluationDate;
    private String evaluationContent;
}
