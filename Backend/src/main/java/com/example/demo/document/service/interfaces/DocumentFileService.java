package com.example.demo.document.service.interfaces;

import com.example.demo.document.dto.request.DocumentFileRequest;
import com.example.demo.document.dto.response.DocumentFileResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface DocumentFileService {

    DocumentFileResponse create(DocumentFileRequest request);


    List<DocumentFileResponse> getByDocumentId(Long documentId);

    void delete(Long fileId);

    CompletableFuture<List<DocumentFileResponse>> getByDocumentIdAsync(Long documentId);
}

