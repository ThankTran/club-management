package com.example.demo.document.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo.document.dto.request.DocumentRequest;
import com.example.demo.document.dto.response.DocumentResponse;
import com.example.demo.document.service.interfaces.DocumentService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

class DocumentControllerTest {
    @Test
    void memberCannotCreateDocumentForAnotherMember() {
        DocumentService documentService = Mockito.mock(DocumentService.class);
        DocumentController controller = new DocumentController(documentService);
        DocumentRequest request = validRequest();
        request.setProposedById(8L);

        var response = controller.create(request, 7L, false);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(documentService, never()).create(any());
    }

    @Test
    void memberCreateDocumentDefaultsProposerToCurrentMember() {
        DocumentService documentService = Mockito.mock(DocumentService.class);
        DocumentController controller = new DocumentController(documentService);
        DocumentRequest request = validRequest();
        when(documentService.create(any())).thenReturn(DocumentResponse.builder().documentId(1L).build());

        var response = controller.create(request, 7L, false);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        ArgumentCaptor<DocumentRequest> captor = ArgumentCaptor.forClass(DocumentRequest.class);
        verify(documentService).create(captor.capture());
        assertEquals(7L, captor.getValue().getProposedById());
    }

    private DocumentRequest validRequest() {
        DocumentRequest request = new DocumentRequest();
        request.setDocumentName("Clean Architecture Notes");
        request.setTypeId(1);
        request.setSubjectId(1);
        request.setSource("https://example.test/notes");
        return request;
    }
}
