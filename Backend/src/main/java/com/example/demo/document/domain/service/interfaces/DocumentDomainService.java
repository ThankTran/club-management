package com.example.demo.document.domain.service.interfaces;

import com.example.demo.document.dto.request.DocumentRequest;
import com.example.demo.member.entity.Member;

public interface DocumentDomainService {
    void validateCreateRequest(DocumentRequest request);

    void validateDocumentUniqueness(String documentName, Integer typeId, Integer subjectId, boolean exists);

    void validateProposer(Member proposedBy);
}
