package com.example.demo.document.mapper;

import com.example.demo.document.dto.request.DocumentTypeRequest;
import com.example.demo.document.dto.response.DocumentTypeResponse;
import com.example.demo.document.entity.DocumentType;
import org.springframework.stereotype.Component;

@Component
public class DocumentTypeMapper {

    public DocumentType toEntity(DocumentTypeRequest request) {
        return DocumentType.builder()
                .typeName(request.getTypeName())
                .build();
    }

    public DocumentTypeResponse toResponse(DocumentType entity) {
        return DocumentTypeResponse.builder()
                .typeId(entity.getTypeId())
                .typeName(entity.getTypeName())
                .build();
    }
}
