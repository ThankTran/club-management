package com.example.demo.event.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventEvaluationResponse {
    private String eventId;
    private String eventName;
    private Long evaluatedById;
    private LocalDateTime evaluationDate;
    private String evaluationContent;
}
