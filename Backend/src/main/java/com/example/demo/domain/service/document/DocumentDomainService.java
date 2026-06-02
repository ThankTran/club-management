package com.example.demo.domain.service.document;

import com.example.demo.application.dto.request.document.DocumentRequest;
import com.example.demo.domain.model.member.Member;

public interface DocumentDomainService {
    void validateCreateRequest(DocumentRequest request);

    void validateDocumentUniqueness(String documentName, Integer typeId, Integer subjectId, boolean exists);

    void validateProposer(Member proposedBy);
}
