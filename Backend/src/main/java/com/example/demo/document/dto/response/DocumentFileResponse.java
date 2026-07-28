package com.example.demo.document.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DocumentFileResponse {
    private Long fileId;
    private Long documentId;
    private String fileUrl;
    private String fileName;
    private Long fileSize;
    private String mimeType;
    private LocalDateTime uploadedAt;
}

