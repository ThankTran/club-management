package com.example.demo.subject.service.impl;

import com.example.demo.subject.dto.request.SubjectRequest;
import com.example.demo.subject.dto.response.SubjectResponse;
import com.example.demo.subject.mapper.SubjectMapper;
import com.example.demo.document.repository.DocumentRepository;
import com.example.demo.subject.repository.SubjectRepository;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;

@Service
@Transactional
@CacheConfig(cacheNames = "subjects")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SubjectServiceImpl implements com.example.demo.subject.service.interfaces.SubjectService {
    SubjectRepository subjectRepository;
    DocumentRepository documentRepository;
    SubjectMapper subjectMapper;

    @CacheEvict(allEntries = true)
    public SubjectResponse create(SubjectRequest request) {
        if (subjectRepository.existsBySubjectNameIgnoreCase(request.getSubjectName())) {
            throw new IllegalArgumentException("Subject already exists: " + request.getSubjectName());
        }
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
        if (id == null) {
            throw new IllegalArgumentException("Subject id must not be empty");
        }
        var subject = subjectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found: " + id));
        String subjectName = request.getSubjectName().trim();
        boolean duplicate = subjectRepository.findBySubjectNameIgnoreCase(subjectName)
                .filter(existing -> !existing.getSubjectId().equals(id))
                .isPresent();

        if (duplicate) {
            throw new IllegalArgumentException("Subject already exists: " + subjectName);
        }
        subject.setSubjectName(subjectName);
        return subjectMapper.toResponse(subjectRepository.save(subject));
    }

    @CacheEvict(allEntries = true)
    public void delete(Integer id) {
        if (!subjectRepository.existsById(id)) {
            throw new IllegalArgumentException("Subject not found: " + id);
        }
        if (documentRepository.existsBySubjectSubjectId(id)) {
            throw new IllegalArgumentException("Cannot delete subject because documents still reference it.");
        }
        subjectRepository.deleteById(id);
    }

    @Async("applicationTaskExecutor")
    public CompletableFuture<List<SubjectResponse>> getAllAsync() {
        return CompletableFuture.completedFuture(getAll());
    }
}
