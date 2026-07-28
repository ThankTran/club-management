package com.example.demo.event.service.interfaces;

import com.example.demo.event.dto.request.EventOrganizerRequest;
import com.example.demo.event.dto.response.EventOrganizerResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface EventOrganizerService {
    EventOrganizerResponse create(EventOrganizerRequest request);

    List<EventOrganizerResponse> getByEvent(String eventId);

    List<EventOrganizerResponse> getByMember(Long memberId);

    void delete(String eventId, Long memberId);

    CompletableFuture<List<EventOrganizerResponse>> getByEventAsync(String eventId);
}
