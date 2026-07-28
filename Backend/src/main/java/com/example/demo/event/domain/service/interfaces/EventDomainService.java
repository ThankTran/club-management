package com.example.demo.event.domain.service.interfaces;

import com.example.demo.event.dto.request.EventRequest;
import java.time.LocalDate;

public interface EventDomainService {
    void validateCreateRequest(EventRequest request);

    void validateUpdateRequest(String eventId, EventRequest request);

    void validateEventNameUniqueness(String eventName, boolean exists);

    void validateDateRange(LocalDate from, LocalDate to);
}
