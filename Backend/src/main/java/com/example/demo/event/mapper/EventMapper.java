package com.example.demo.event.mapper;

import com.example.demo.event.dto.request.EventRequest;
import com.example.demo.event.dto.response.EventResponse;
import com.example.demo.shared.enums.ApprovalStatusEnum;
import com.example.demo.shared.enums.EventStatusEnum;
import com.example.demo.event.entity.Event;
import com.example.demo.member.entity.Member;
import org.springframework.stereotype.Component;

@Component
public class EventMapper {

    public Event toEntity(EventRequest request, Member evaluatedBy) {
        return Event.builder()
                .eventId(request.getEventId())
                .eventName(request.getEventName())
                .location(request.getLocation())
                .eventDate(request.getEventDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .estimatedCost(request.getEstimatedCost())
                .capacity(request.getCapacity())
                .organizer(request.getOrganizer())
                .tag(request.getTag())
                .status(request.getStatus() != null ? request.getStatus() : EventStatusEnum.NotStarted)
                .reqStatus(request.getReqStatus() != null ? request.getReqStatus() : ApprovalStatusEnum.PENDING)
                .description(request.getDescription())
                .evaluatedBy(evaluatedBy)
                .evaluationDate(request.getEvaluationDate())
                .evaluationContent(request.getEvaluationContent())
                .build();
    }

    public EventResponse toResponse(Event entity) {
        Long evaluatedById = entity.getEvaluatedBy() == null ? null : entity.getEvaluatedBy().getMemberId();
        return EventResponse.builder()
                .eventId(entity.getEventId())
                .eventName(entity.getEventName())
                .location(entity.getLocation())
                .eventDate(entity.getEventDate())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .estimatedCost(entity.getEstimatedCost())
                .capacity(entity.getCapacity())
                .organizer(entity.getOrganizer())
                .tag(entity.getTag())
                .status(entity.getStatus())
                .reqStatus(entity.getReqStatus())
                .description(entity.getDescription())
                .evaluatedById(evaluatedById)
                .evaluationDate(entity.getEvaluationDate())
                .evaluationContent(entity.getEvaluationContent())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
