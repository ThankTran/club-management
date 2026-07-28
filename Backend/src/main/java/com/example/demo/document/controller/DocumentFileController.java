package com.example.demo.document.controller;

import com.example.demo.document.dto.request.DocumentFileRequest;
import com.example.demo.document.dto.response.DocumentFileResponse;
import com.example.demo.document.dto.response.DocumentResponse;
import com.example.demo.document.service.interfaces.DocumentFileService;
import com.example.demo.document.service.interfaces.DocumentService;
import com.example.demo.shared.security.AccessControlInterceptor;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/document-files")
public class DocumentFileController {

    private final DocumentFileService documentFileService;
    private final DocumentService documentService;

    public DocumentFileController(DocumentFileService documentFileService, DocumentService documentService) {
        this.documentFileService = documentFileService;
        this.documentService = documentService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> create(
            @Valid @ModelAttribute DocumentFileRequest request,
            @RequestAttribute(value = AccessControlInterceptor.CURRENT_MEMBER_ID_ATTRIBUTE, required = false) Long currentMemberId,
            @RequestAttribute(value = AccessControlInterceptor.CURRENT_USER_IS_MANAGER_ATTRIBUTE, required = false) Boolean currentUserIsManager) {
        try {
            if (!Boolean.TRUE.equals(currentUserIsManager)) {
                if (currentMemberId == null) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Không xác định được thành viên hiện tại");
                }
                DocumentResponse document = documentService.getById(request.getDocumentId());
                if (!Objects.equals(document.getProposedById(), currentMemberId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Bạn không có quyền tải tệp lên tài liệu này");
                }
            }
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(documentFileService.create(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/by-document/{documentId}")
    public ResponseEntity<List<DocumentFileResponse>> listByDocument(@PathVariable Long documentId) {
        return ResponseEntity.ok(documentFileService.getByDocumentId(documentId));
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> delete(@PathVariable Long fileId) {
        documentFileService.delete(fileId);
        return ResponseEntity.noContent().build();
    }
}
