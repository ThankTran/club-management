package com.example.demo.document.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DocumentTypeRequest {
    @NotBlank(message = "Type name must not be blank")
    @Size(max = 100, message = "Type name must not exceed 100 characters")
    private String typeName;
}
