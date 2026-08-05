package com.example.demo.document.mapper;

import com.example.demo.document.dto.request.DocumentRequest;
import com.example.demo.document.dto.response.DocumentResponse;
import com.example.demo.document.entity.Document;
import com.example.demo.document.entity.DocumentFile;
import com.example.demo.document.entity.DocumentType;
import com.example.demo.member.entity.Member;
import com.example.demo.shared.config.GlobalMapperConfig;
import com.example.demo.subject.entity.Subject;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfig.class)
public abstract class DocumentMapper {
    @Mapping(target = "type", source = "type")
    @Mapping(target = "subject", source = "subject")
    @Mapping(target = "proposedBy", source = "proposedBy")
    public abstract Document toEntity(DocumentRequest request, DocumentType type, Subject subject, Member proposedBy);

    @Mapping(source = "type.typeId", target = "typeId")
    @Mapping(source = "type.typeName", target = "typeName")
    @Mapping(source = "subject.subjectId", target = "subjectId")
    @Mapping(source = "subject.subjectName", target = "subjectName")
    @Mapping(source = "proposedBy.memberId", target = "proposedById")
    @Mapping(source = "approvedBy.memberId", target = "approvedById")
    public abstract DocumentResponse toResponse(Document entity);

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


