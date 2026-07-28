package com.example.demo.document.mapper;

import com.example.demo.document.dto.request.DocumentRequest;
import com.example.demo.document.dto.response.DocumentResponse;
import com.example.demo.document.entity.Document;
import com.example.demo.document.entity.DocumentFile;
import com.example.demo.document.entity.DocumentType;
import com.example.demo.member.entity.Member;
import com.example.demo.subject.entity.Subject;
import org.springframework.stereotype.Component;

@Component
public class DocumentMapper {
    public Document toEntity(DocumentRequest request, DocumentType type, Subject subject, Member proposedBy) {
        return Document.builder()
                .documentName(request.getDocumentName())
                .type(type)
                .subject(subject)
                .source(request.getSource())
                .note(request.getNote())
                .proposedBy(proposedBy)
                .build();
    }

    public DocumentResponse toResponse(Document entity) {
        return DocumentResponse.builder()
                .documentId(entity.getDocumentId())
                .documentName(entity.getDocumentName())
                .typeId(entity.getType() != null ? entity.getType().getTypeId() : null)
                .typeName(entity.getType() != null ? entity.getType().getTypeName() : null)
                .subjectId(entity.getSubject() != null ? entity.getSubject().getSubjectId() : null)
                .subjectName(entity.getSubject() != null ? entity.getSubject().getSubjectName() : null)
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .reqStatus(entity.getReqStatus() != null ? entity.getReqStatus().name() : null)
                .version(entity.getVersion())
                .source(entity.getSource())
                .note(entity.getNote())
                .lookupFolderId(entity.getLookupFolderId())
                .proposedById(entity.getProposedBy() != null ? entity.getProposedBy().getMemberId() : null)
                .approvedById(entity.getApprovedBy() != null ? entity.getApprovedBy().getMemberId() : null)
                .approvedAt(entity.getApprovedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public DocumentResponse toResponse(Document entity, DocumentFile primaryFile) {
        DocumentResponse response = toResponse(entity);
        if (primaryFile != null) {
            response.setPrimaryFileUrl(primaryFile.getFileUrl());
            response.setPrimaryFileName(primaryFile.getFileName());
            response.setFileSize(primaryFile.getFileSize());
            response.setMimeType(primaryFile.getMimeType());
        }
        return response;
    }
}

