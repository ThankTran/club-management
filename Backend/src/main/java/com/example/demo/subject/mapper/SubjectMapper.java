package com.example.demo.subject.mapper;

import com.example.demo.subject.dto.request.SubjectRequest;
import com.example.demo.subject.dto.response.SubjectResponse;
import com.example.demo.subject.entity.Subject;
import org.springframework.stereotype.Component;

@Component
public class SubjectMapper {
    public Subject toEntity(SubjectRequest request) {
        return Subject.builder().subjectName(request.getSubjectName()).build();
    }

    public SubjectResponse toResponse(Subject entity) {
        return SubjectResponse.builder()
                .subjectId(entity.getSubjectId())
                .subjectName(entity.getSubjectName())
                .build();
    }
}

