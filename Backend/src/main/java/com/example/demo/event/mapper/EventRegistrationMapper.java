package com.example.demo.event.mapper;

import com.example.demo.event.dto.response.EventRegistrationResponse;
import com.example.demo.event.entity.EventRegistration;
import com.example.demo.member.entity.Member;
import org.springframework.stereotype.Component;

@Component
public class EventRegistrationMapper {
    public EventRegistrationResponse toResponse(EventRegistration entity) {
        Member member = entity.getMember();
        String departmentName = member.getDepartment() == null ? null : member.getDepartment().getDepartmentName();
        return EventRegistrationResponse.builder()
                .eventId(entity.getId() == null ? null : entity.getId().getEventId())
                .memberId(entity.getId() == null ? null : entity.getId().getMemberId())
                .studentId(member.getStudentId())
                .fullName(member.getFullName())
                .departmentName(departmentName)
                .email(member.getEmail())
                .registeredAt(entity.getRegisteredAt())
                .attended(entity.getAttended())
                .attendedAt(entity.getAttendedAt())
                .build();
    }
}
