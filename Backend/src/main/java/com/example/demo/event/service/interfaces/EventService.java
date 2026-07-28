package com.example.demo.event.service.interfaces;

import com.example.demo.event.dto.request.EventRequest;
import com.example.demo.event.dto.request.EventEvaluationRequest;
import com.example.demo.event.dto.response.EventCalendarLinkResponse;
import com.example.demo.event.dto.response.EventEvaluationResponse;
import com.example.demo.event.dto.response.EventPublicResponse;
import com.example.demo.event.dto.response.EventResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface EventService {
    EventResponse create(EventRequest request);

    EventResponse update(String id, EventRequest request);

    List<EventResponse> getAll();

    List<EventPublicResponse> getPublicUpcomingEvents();

    List<EventResponse> searchByName(String eventName);

    List<EventResponse> getByDateRange(LocalDate from, LocalDate to);

    EventResponse getById(String id);

    EventCalendarLinkResponse getGoogleCalendarLink(String id);

    EventEvaluationResponse createOrUpdateEvaluation(EventEvaluationRequest request);

    EventEvaluationResponse getEvaluationByEvent(String eventId);

    void delete(String id);

    CompletableFuture<List<EventResponse>> getAllAsync();

    CompletableFuture<List<EventResponse>> searchByNameAsync(String eventName);

    CompletableFuture<List<EventResponse>> getByDateRangeAsync(LocalDate from, LocalDate to);

    CompletableFuture<EventResponse> getByIdAsync(String id);
}
