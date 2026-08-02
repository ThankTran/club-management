package com.example.demo.subject.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SubjectRequest {
    @NotBlank(message = "Subject name must not be empty")
    private String subjectName;
}

