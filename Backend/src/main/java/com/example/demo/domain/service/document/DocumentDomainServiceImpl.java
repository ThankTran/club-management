package com.example.demo.domain.service.document;

import com.example.demo.application.dto.request.document.DocumentRequest;
import com.example.demo.domain.enums.ApprovalStatusEnum;
import com.example.demo.domain.model.member.Member;
import org.springframework.stereotype.Service;

@Service
public class DocumentDomainServiceImpl implements DocumentDomainService {
    @Override
    public void validateCreateRequest(DocumentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Document request must not be empty");
        }
        if (request.getDocumentName() == null || request.getDocumentName().isBlank()) {
            throw new IllegalArgumentException("Document name must not be empty");
        }
        if (request.getDocumentName().length() > 255) {
            throw new IllegalArgumentException("Document name must not exceed 255 characters");
        }
        if (request.getTypeId() == null) {
            throw new IllegalArgumentException("Document type is required");
        }
        if (request.getSubjectId() == null) {
            throw new IllegalArgumentException("Subject is required");
        }
        if (request.getProposedById() == null) {
            throw new IllegalArgumentException("Proposer is required");
        }
        if (request.getSource() != null && request.getSource().length() > 255) {
            throw new IllegalArgumentException("Source must not exceed 255 characters");
        }
        if (request.getNote() != null && request.getNote().length() > 5000) {
            throw new IllegalArgumentException("Note must not exceed 5000 characters");
        }
    }

    @Override
    public void validateDocumentUniqueness(String documentName, Integer typeId, Integer subjectId, boolean exists) {
        if (exists) {
            throw new IllegalArgumentException(
                    "Document with the same name, type and subject already exists: " + documentName);
        }
    }

    @Override
    public void validateProposer(Member proposedBy) {
        if (proposedBy == null) {
            throw new IllegalArgumentException("Proposer must exist");
        }
        if (proposedBy.getReqStatus() != ApprovalStatusEnum.APPROVED) {
            throw new IllegalArgumentException("Only approved club members can propose documents");
        }
    }
}
