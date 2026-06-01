package com.example.demo.application.mapper.document;

import com.example.demo.application.dto.request.document.DocumentFileRequest;
import com.example.demo.application.dto.response.document.DocumentFileResponse;
import com.example.demo.domain.model.document.Document;
import com.example.demo.domain.model.document.DocumentFile;
import org.springframework.stereotype.Component;

@Component
public class DocumentFileMapper {
    public DocumentFile toEntity(DocumentFileRequest request, Document document, String url) {
        boolean hasUploadedFile = request.getFile() != null && !request.getFile().isEmpty();
        String fileName = hasUploadedFile
                ? request.getFile().getOriginalFilename()
                : request.getFileName();
        Long fileSize = hasUploadedFile
                ? request.getFile().getSize()
                : request.getFileSize();
        String mimeType = hasUploadedFile
                ? request.getFile().getContentType()
                : request.getMimeType();

        return DocumentFile.builder()
                .document(document)
                .fileUrl(url)
                .fileName(fileName)
                .fileSize(fileSize)
                .mimeType(mimeType)
                .build();
    }

    public DocumentFileResponse toResponse(DocumentFile entity) {
        return DocumentFileResponse.builder()
                .fileId(entity.getFileId())
                .documentId(entity.getDocument() != null ? entity.getDocument().getDocumentId() : null)
                .fileUrl(entity.getFileUrl())
                .fileName(entity.getFileName())
                .fileSize(entity.getFileSize())
                .mimeType(entity.getMimeType())
                .uploadedAt(entity.getUploadedAt())
                .build();
    }
}

