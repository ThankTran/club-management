package com.example.demo.subject.domain.service.interfaces;

import com.example.demo.subject.dto.request.SubjectRequest;

public interface SubjectDomainService {
    void validateCreateRequest(SubjectRequest request);

    void validateUpdateRequest(Integer subjectId, SubjectRequest request);

    void validateSubjectUniqueness(String subjectName, boolean exists);

    void validateDelete(Integer subjectId, boolean exists, boolean hasDocuments);
}
