package com.example.demo.subject.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubjectResponse {
    private Integer subjectId;
    private String subjectName;
}

