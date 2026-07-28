package com.example.demo.event.service.interfaces;

import com.example.demo.event.dto.request.EventAttendanceRequest;
import com.example.demo.event.dto.response.EventRegistrationResponse;
import java.util.List;

public interface EventRegistrationService {
    EventRegistrationResponse register(String eventId, Long memberId);

    List<EventRegistrationResponse> getByEvent(String eventId);

    List<EventRegistrationResponse> getByMember(Long memberId);

    List<EventRegistrationResponse> updateAttendance(String eventId, EventAttendanceRequest request);

    void unregister(String eventId, Long memberId);
}
