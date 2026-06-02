package com.example.demo.application.service.document.interfaces;

import com.example.demo.application.dto.request.document.DocumentApprovalRequest;
import com.example.demo.application.dto.request.document.DocumentRequest;
import com.example.demo.application.dto.response.document.DocumentResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface DocumentService {

    DocumentResponse create(DocumentRequest request);

    List<DocumentResponse> getAll();

    List<DocumentResponse> getAll(String reqStatus, String lookupFolderId, Integer typeId, Integer subjectId, String name);

    DocumentResponse approve(DocumentApprovalRequest request);

    DocumentResponse moveLookupFolder(Long documentId, String lookupFolderId);

    List<DocumentResponse> searchByName(String documentName);

    List<DocumentResponse> getBySubject(Integer subjectId);

    List<DocumentResponse> getByType(Integer typeId);

    DocumentResponse getById(Long id);

    void softDeleteById(Long id);

    void hardDeleteById(Long id);

    CompletableFuture<List<DocumentResponse>> getAllAsync();

    CompletableFuture<DocumentResponse> getByIdAsync(Long id);
}

