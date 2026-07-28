package com.example.demo.document.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class DocumentFileRequest {
    @NotNull(message = "Document id is required")
    private Long documentId;

    private MultipartFile file;

    private String fileUrl;
    private String fileName;
    private Long fileSize;
    private String mimeType;
}
