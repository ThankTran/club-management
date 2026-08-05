package com.example.demo.document.mapper;

import com.example.demo.document.dto.request.DocumentFileRequest;
import com.example.demo.document.dto.response.DocumentFileResponse;
import com.example.demo.document.entity.Document;
import com.example.demo.document.entity.DocumentFile;
import com.example.demo.shared.config.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfig.class)
public abstract class DocumentFileMapper {
    /**
     * Intentionally manual: field values (fileName, fileSize, mimeType) are derived
     * conditionally from either a {@link org.springframework.web.multipart.MultipartFile}
     * or DTO fallback fields. This conditional logic cannot be expressed with
     * {@code @Mapping} annotations and must remain as a concrete method.
     */
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

    @Mapping(source = "document.documentId", target = "documentId")
    public abstract DocumentFileResponse toResponse(DocumentFile entity);
}

