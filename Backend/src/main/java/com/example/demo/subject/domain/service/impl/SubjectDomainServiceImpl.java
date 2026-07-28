package com.example.demo.subject.domain.service.impl;

import com.example.demo.subject.domain.service.interfaces.SubjectDomainService;
import com.example.demo.subject.dto.request.SubjectRequest;
import org.springframework.stereotype.Service;

@Service
public class SubjectDomainServiceImpl implements SubjectDomainService {
    @Override
    public void validateCreateRequest(SubjectRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Subject request must not be empty");
        }
        if (request.getSubjectName() == null || request.getSubjectName().isBlank()) {
            throw new IllegalArgumentException("Subject name must not be empty");
        }
    }

    @Override
    public void validateUpdateRequest(Integer subjectId, SubjectRequest request) {
        if (subjectId == null) {
            throw new IllegalArgumentException("Subject id must not be empty");
        }
        validateCreateRequest(request);
    }

    @Override
    public void validateSubjectUniqueness(String subjectName, boolean exists) {
        if (exists) {
            throw new IllegalArgumentException("Subject already exists: " + subjectName);
        }
    }

    @Override
    public void validateDelete(Integer subjectId, boolean exists, boolean hasDocuments) {
        if (!exists) {
            throw new IllegalArgumentException("Subject not found: " + subjectId);
        }
        if (hasDocuments) {
            throw new IllegalArgumentException("Cannot delete subject because documents still reference it.");
        }
    }
}
