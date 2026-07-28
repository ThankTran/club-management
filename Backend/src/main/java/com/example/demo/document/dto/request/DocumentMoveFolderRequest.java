package com.example.demo.document.dto.request;

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
