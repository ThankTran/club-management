package com.example.demo.event.mapper;

import com.example.demo.event.dto.request.EventOrganizerRequest;
import com.example.demo.event.dto.response.EventOrganizerResponse;
import com.example.demo.event.entity.Event;
import com.example.demo.event.entity.EventOrganizer;
import com.example.demo.event.entity.EventOrganizerId;
import com.example.demo.event.entity.EventRole;
import com.example.demo.member.entity.Member;
import com.example.demo.shared.config.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfig.class)
public abstract class EventOrganizerMapper {

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

    @Mapping(source = "id.eventId", target = "eventId")
    @Mapping(source = "id.memberId", target = "memberId")
    @Mapping(source = "role.roleId", target = "roleId")
    @Mapping(source = "role.roleName", target = "roleName")
    public abstract EventOrganizerResponse toResponse(EventOrganizer entity);
}

