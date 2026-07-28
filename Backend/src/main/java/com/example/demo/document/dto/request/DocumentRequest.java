package com.example.demo.document.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DocumentRequest {

    @NotBlank(message = "Document name must not be blank")
    @Size(max = 255, message = "Document name must not exceed 255 characters")
    private String documentName;

    @NotNull(message = "Document type is required")
    private Integer typeId;

    @NotNull(message = "Subject is required")
    private Integer subjectId;

    @Size(max = 255, message = "Source must not exceed 255 characters")
    private String source;

    @Size(max = 5000, message = "Note must not exceed 5000 characters")
    private String note;

    private Long proposedById;
}

