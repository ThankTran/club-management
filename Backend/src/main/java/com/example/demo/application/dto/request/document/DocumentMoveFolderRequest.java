package com.example.demo.application.dto.request.document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DocumentMoveFolderRequest {
    @NotNull(message = "Document id is required")
    private Long documentId;

    @NotBlank(message = "Lookup folder id is required")
    private String lookupFolderId;
}
