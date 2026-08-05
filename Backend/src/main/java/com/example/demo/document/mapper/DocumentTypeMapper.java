package com.example.demo.document.mapper;

import com.example.demo.document.dto.request.DocumentTypeRequest;
import com.example.demo.document.dto.response.DocumentTypeResponse;
import com.example.demo.document.entity.DocumentType;
import com.example.demo.shared.config.GlobalMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = GlobalMapperConfig.class)
public interface DocumentTypeMapper {
    DocumentType toEntity(DocumentTypeRequest request);
    DocumentTypeResponse toResponse(DocumentType entity);
}

