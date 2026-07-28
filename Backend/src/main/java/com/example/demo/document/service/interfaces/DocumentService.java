package com.example.demo.document.service.interfaces;

import com.example.demo.document.dto.request.DocumentApprovalRequest;
import com.example.demo.document.dto.request.DocumentRequest;
import com.example.demo.document.dto.response.DocumentResponse;
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

