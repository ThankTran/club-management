package com.example.demo.document.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo.document.dto.request.DocumentFileRequest;
import com.example.demo.document.dto.response.DocumentFileResponse;
import com.example.demo.document.dto.response.DocumentResponse;
import com.example.demo.document.service.interfaces.DocumentFileService;
import com.example.demo.document.service.interfaces.DocumentService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

class DocumentFileControllerTest {
    @Test
    void memberCannotUploadFileToAnotherMembersDocument() {
        DocumentFileService fileService = Mockito.mock(DocumentFileService.class);
        DocumentService documentService = Mockito.mock(DocumentService.class);
        DocumentFileController controller = new DocumentFileController(fileService, documentService);
        DocumentFileRequest request = new DocumentFileRequest();
        request.setDocumentId(10L);
        when(documentService.getById(10L)).thenReturn(DocumentResponse.builder()
                .documentId(10L)
                .proposedById(8L)
                .build());

        var response = controller.create(request, 7L, false);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(fileService, never()).create(any());
    }

    @Test
    void managerCanUploadFileToAnyDocument() {
        DocumentFileService fileService = Mockito.mock(DocumentFileService.class);
        DocumentService documentService = Mockito.mock(DocumentService.class);
        DocumentFileController controller = new DocumentFileController(fileService, documentService);
        DocumentFileRequest request = new DocumentFileRequest();
        request.setDocumentId(10L);
        when(documentService.getById(10L)).thenReturn(DocumentResponse.builder()
                .documentId(10L)
                .proposedById(8L)
                .build());
        when(fileService.create(request)).thenReturn(DocumentFileResponse.builder().fileId(5L).build());

        var response = controller.create(request, 7L, true);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(fileService).create(request);
    }
}
