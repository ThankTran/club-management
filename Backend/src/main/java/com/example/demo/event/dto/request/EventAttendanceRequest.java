package com.example.demo.event.dto.request;

import java.util.List;
import lombok.Data;

@Data
public class EventAttendanceRequest {
    private Long memberId;
    private List<Long> memberIds;
    private Boolean attended;
}
