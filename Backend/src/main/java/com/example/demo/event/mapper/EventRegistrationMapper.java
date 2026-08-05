package com.example.demo.event.mapper;

import com.example.demo.event.dto.response.EventRegistrationResponse;
import com.example.demo.event.entity.EventRegistration;
import com.example.demo.shared.config.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfig.class)
public interface EventRegistrationMapper {
    @Mapping(source = "id.eventId", target = "eventId")
    @Mapping(source = "id.memberId", target = "memberId")
    @Mapping(source = "member.studentId", target = "studentId")
    @Mapping(source = "member.fullName", target = "fullName")
    @Mapping(source = "member.department.departmentName", target = "departmentName")
    @Mapping(source = "member.email", target = "email")
    EventRegistrationResponse toResponse(EventRegistration entity);
}

