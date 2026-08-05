package com.example.demo.event.mapper;

import com.example.demo.event.dto.request.EventRequest;
import com.example.demo.event.dto.response.EventResponse;
import com.example.demo.shared.config.GlobalMapperConfig;
import com.example.demo.shared.enums.ApprovalStatusEnum;
import com.example.demo.shared.enums.EventStatusEnum;
import com.example.demo.event.entity.Event;
import com.example.demo.member.entity.Member;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfig.class, imports = {EventStatusEnum.class, ApprovalStatusEnum.class})
public interface EventMapper {

    @Mapping(target = "evaluatedBy", source = "evaluatedBy")
    @Mapping(target = "status",    source = "request.status",
             defaultExpression = "java(EventStatusEnum.NotStarted)")
    @Mapping(target = "reqStatus", source = "request.reqStatus",
             defaultExpression = "java(ApprovalStatusEnum.PENDING)")
    Event toEntity(EventRequest request, Member evaluatedBy);

    @Mapping(source = "evaluatedBy.memberId", target = "evaluatedById")
    EventResponse toResponse(Event entity);
}
