package com.example.demo.event.dto.request;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class EventEvaluationRequest {
    private String eventId;
    private Long evaluatedById;
    private LocalDateTime evaluationDate;
    private String evaluationContent;
}
