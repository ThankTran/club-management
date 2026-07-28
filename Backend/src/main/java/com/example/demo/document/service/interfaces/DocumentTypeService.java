package com.example.demo.document.service.interfaces;

import com.example.demo.document.dto.request.DocumentTypeRequest;
import com.example.demo.document.dto.response.DocumentTypeResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface DocumentTypeService {

    DocumentTypeResponse create(DocumentTypeRequest request);

    List<DocumentTypeResponse> getAll();

    DocumentTypeResponse getByName(String typeName);

    CompletableFuture<List<DocumentTypeResponse>> getAllAsync();
}

