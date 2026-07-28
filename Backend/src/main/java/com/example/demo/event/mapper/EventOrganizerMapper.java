package com.example.demo.event.mapper;

import com.example.demo.event.dto.request.EventOrganizerRequest;
import com.example.demo.event.dto.response.EventOrganizerResponse;
import com.example.demo.event.entity.Event;
import com.example.demo.event.entity.EventOrganizer;
import com.example.demo.event.entity.EventOrganizerId;
import com.example.demo.event.entity.EventRole;
import com.example.demo.member.entity.Member;
import org.springframework.stereotype.Component;

@Component
public class EventOrganizerMapper {

    public EventOrganizer toEntity(
            EventOrganizerRequest request,
            Event event,
            Member member,
            EventRole role) {
        EventOrganizerId id = new EventOrganizerId(request.getEventId(), request.getMemberId());
        return EventOrganizer.builder()
                .id(id)
                .event(event)
                .member(member)
                .role(role)
                .build();
    }

    public EventOrganizerResponse toResponse(EventOrganizer entity) {
        String eventId = entity.getId() != null ? entity.getId().getEventId() : null;
        Long memberId = entity.getId() != null ? entity.getId().getMemberId() : null;
        Short roleId = entity.getRole() == null ? null : entity.getRole().getRoleId();
        String roleName = entity.getRole() == null ? null : entity.getRole().getRoleName();
        return EventOrganizerResponse.builder()
                .eventId(eventId)
                .memberId(memberId)
                .roleId(roleId)
                .roleName(roleName)
                .build();
    }
}
