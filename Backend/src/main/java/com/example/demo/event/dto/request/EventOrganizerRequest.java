package com.example.demo.event.dto.request;

import lombok.Data;

@Data
public class EventOrganizerRequest {
    private String eventId;
    private Long memberId;
    private Short roleId;
}
