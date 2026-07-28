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
public class EventRegistrationResponse {
    private String eventId;
    private Long memberId;
    private String studentId;
    private String fullName;
    private String departmentName;
    private String email;
    private LocalDateTime registeredAt;
    private Boolean attended;
    private LocalDateTime attendedAt;
}
