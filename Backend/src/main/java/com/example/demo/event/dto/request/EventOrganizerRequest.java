package com.example.demo.event.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EventOrganizerRequest {
    @NotBlank(message = "Event ID must not be empty")
    private String eventId;

    @NotNull(message = "Member ID must not be empty")
    private Long memberId;

    @NotNull(message = "Event role ID must not be empty")
    private Short roleId;
}
