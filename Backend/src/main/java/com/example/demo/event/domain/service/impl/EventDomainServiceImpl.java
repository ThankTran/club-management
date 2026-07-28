package com.example.demo.event.domain.service.impl;

import com.example.demo.event.domain.service.interfaces.EventDomainService;
import com.example.demo.event.dto.request.EventRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

@Service
public class EventDomainServiceImpl implements EventDomainService {
    @Override
    public void validateCreateRequest(EventRequest request) {
        validateEventPayload(request, true);
        if (request.getEventId() == null || request.getEventId().isBlank()) {
            throw new IllegalArgumentException("Event ID must not be empty");
        }
        if (request.getEventDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Event date must be today or later");
        }
    }

    @Override
    public void validateUpdateRequest(String eventId, EventRequest request) {
        if (eventId == null || eventId.isBlank()) {
            throw new IllegalArgumentException("Event ID must not be empty");
        }
        validateEventPayload(request, false);
        if (request.getEventId() != null && !request.getEventId().isBlank()
                && !eventId.equals(request.getEventId())) {
            throw new IllegalArgumentException("Event ID in path and body must match");
        }
    }

    private void validateEventPayload(EventRequest request, boolean validateEvaluationDate) {
        if (request == null) {
            throw new IllegalArgumentException("Event request must not be empty");
        }
        if (request.getEventName() == null || request.getEventName().isBlank()) {
            throw new IllegalArgumentException("Event name must not be empty");
        }
        if (request.getEventDate() == null) {
            throw new IllegalArgumentException("Event date must not be empty");
        }
        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new IllegalArgumentException("Start time and end time must not be empty");
        }
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        if (request.getEstimatedCost() != null && request.getEstimatedCost().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Estimated cost must not be negative");
        }
        if (request.getCapacity() != null && request.getCapacity() < 0) {
            throw new IllegalArgumentException("Capacity must not be negative");
        }
        if (validateEvaluationDate
                && request.getEvaluationDate() != null
                && !request.getEvaluationDate().isAfter(request.getEndTime())) {
            throw new IllegalArgumentException("Evaluation date must be after event end time");
        }
    }

    @Override
    public void validateEventNameUniqueness(String eventName, boolean exists) {
        if (exists) {
            throw new IllegalArgumentException("Event name already exists: " + eventName);
        }
    }

    @Override
    public void validateDateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Date range must not be empty");
        }
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("End date must be on or after start date");
        }
    }
}
