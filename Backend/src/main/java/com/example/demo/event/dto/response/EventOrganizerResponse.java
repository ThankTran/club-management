package com.example.demo.event.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventOrganizerResponse {
    private String eventId;
    private Long memberId;
    private Short roleId;
    private String roleName;
}
