package com.example.demo.document.service.impl;

import com.example.demo.document.dto.request.DocumentTypeRequest;
import com.example.demo.document.dto.response.DocumentTypeResponse;
import com.example.demo.document.mapper.DocumentTypeMapper;
import com.example.demo.document.service.interfaces.DocumentTypeService;
import com.example.demo.document.repository.DocumentTypeRepository;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@CacheConfig(cacheNames = "documentTypes")
public class DocumentTypeServiceImpl implements DocumentTypeService {
    private final DocumentTypeRepository documentTypeRepository;
    private final DocumentTypeMapper documentTypeMapper;

    public DocumentTypeServiceImpl(
            DocumentTypeRepository documentTypeRepository,
            DocumentTypeMapper documentTypeMapper) {
        this.documentTypeRepository = documentTypeRepository;
        this.documentTypeMapper = documentTypeMapper;
    }

    @Override
    @CacheEvict(allEntries = true)
    public DocumentTypeResponse create(DocumentTypeRequest request) {
        return documentTypeMapper.toResponse(
                documentTypeRepository.save(documentTypeMapper.toEntity(request)));
    }

    @Override
    @Cacheable(key = "'all'")
    public List<DocumentTypeResponse> getAll() {
        return documentTypeRepository.findAll().stream()
                .map(documentTypeMapper::toResponse)
                .toList();
    }

    @Override
    @Cacheable(key = "'name:' + #typeName")
    public DocumentTypeResponse getByName(String typeName) {
        return documentTypeRepository.findByTypeNameIgnoreCase(typeName)
                .map(documentTypeMapper::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy loại tài liệu: " + typeName));
    }

    @Override
    @Async("applicationTaskExecutor")
    public CompletableFuture<List<DocumentTypeResponse>> getAllAsync() {
        return CompletableFuture.completedFuture(getAll());
    }
}

