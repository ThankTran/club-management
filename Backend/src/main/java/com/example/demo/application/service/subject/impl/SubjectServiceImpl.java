package com.example.demo.application.service.subject.impl;

import com.example.demo.application.dto.request.subject.SubjectRequest;
import com.example.demo.application.dto.response.subject.SubjectResponse;
import com.example.demo.application.mapper.subject.SubjectMapper;
import com.example.demo.domain.repository.document.DocumentRepository;
import com.example.demo.domain.repository.subject.SubjectRepository;
import com.example.demo.domain.service.subject.SubjectDomainService;
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
@CacheConfig(cacheNames = "subjects")
public class SubjectServiceImpl implements com.example.demo.application.service.subject.interfaces.SubjectService {
    private final SubjectRepository subjectRepository;
    private final DocumentRepository documentRepository;
    private final SubjectMapper subjectMapper;
    private final SubjectDomainService subjectDomainService;

    public SubjectServiceImpl(
            SubjectRepository subjectRepository,
            DocumentRepository documentRepository,
            SubjectMapper subjectMapper,
            SubjectDomainService subjectDomainService) {
        this.subjectRepository = subjectRepository;
        this.documentRepository = documentRepository;
        this.subjectMapper = subjectMapper;
        this.subjectDomainService = subjectDomainService;
    }

    @CacheEvict(allEntries = true)
    public SubjectResponse create(SubjectRequest request) {
        subjectDomainService.validateCreateRequest(request);
        subjectDomainService.validateSubjectUniqueness(
                request.getSubjectName(),
                subjectRepository.existsBySubjectNameIgnoreCase(request.getSubjectName()));
        return subjectMapper.toResponse(subjectRepository.save(subjectMapper.toEntity(request)));
    }

    @Cacheable(key = "'all'")
    public List<SubjectResponse> getAll() {
        return subjectRepository.findAll().stream().map(subjectMapper::toResponse).toList();
    }

    @Cacheable(key = "'id:' + #id")
    public SubjectResponse getById(Integer id) {
        return subjectRepository.findById(id).map(subjectMapper::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chủ đề: " + id));
    }

    @CacheEvict(allEntries = true)
    public SubjectResponse update(Integer id, SubjectRequest request) {
        subjectDomainService.validateUpdateRequest(id, request);
        var subject = subjectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found: " + id));
        String subjectName = request.getSubjectName().trim();
        boolean duplicate = subjectRepository.findBySubjectNameIgnoreCase(subjectName)
                .filter(existing -> !existing.getSubjectId().equals(id))
                .isPresent();

        subjectDomainService.validateSubjectUniqueness(subjectName, duplicate);
        subject.setSubjectName(subjectName);
        return subjectMapper.toResponse(subjectRepository.save(subject));
    }

    @CacheEvict(allEntries = true)
    public void delete(Integer id) {
        subjectDomainService.validateDelete(
                id,
                subjectRepository.existsById(id),
                documentRepository.existsBySubjectSubjectId(id));
        subjectRepository.deleteById(id);
    }

    @Async("applicationTaskExecutor")
    public CompletableFuture<List<SubjectResponse>> getAllAsync() {
        return CompletableFuture.completedFuture(getAll());
    }
}
