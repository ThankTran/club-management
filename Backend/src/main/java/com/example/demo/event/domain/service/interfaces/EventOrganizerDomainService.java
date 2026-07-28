package com.example.demo.event.domain.service.interfaces;

import com.example.demo.event.dto.request.EventOrganizerRequest;

public interface EventOrganizerDomainService {
    void validateCreateRequest(EventOrganizerRequest request);

    void validateAssignmentUniqueness(String eventId, Long memberId, boolean exists);

    void validateDelete(String eventId, Long memberId);
}
