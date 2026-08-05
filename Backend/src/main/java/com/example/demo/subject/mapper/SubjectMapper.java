package com.example.demo.subject.mapper;

import com.example.demo.shared.config.GlobalMapperConfig;
import com.example.demo.subject.dto.request.SubjectRequest;
import com.example.demo.subject.dto.response.SubjectResponse;
import com.example.demo.subject.entity.Subject;
import org.mapstruct.Mapper;

@Mapper(config = GlobalMapperConfig.class)
public interface SubjectMapper {
    Subject toEntity(SubjectRequest request);
    SubjectResponse toResponse(Subject entity);
}


