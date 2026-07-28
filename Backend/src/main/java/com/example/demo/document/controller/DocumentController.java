package com.example.demo.document.controller;

import com.example.demo.document.dto.request.DocumentApprovalRequest;
import com.example.demo.document.dto.request.DocumentMoveFolderRequest;
import com.example.demo.document.dto.request.DocumentRequest;
import com.example.demo.document.dto.response.DocumentResponse;
import com.example.demo.shared.exception.BusinessException;
import com.example.demo.document.service.interfaces.DocumentService;
import com.example.demo.shared.security.AccessControlInterceptor;
import java.util.List;
import java.util.Objects;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping
    public ResponseEntity<?> create(
            @Valid @RequestBody DocumentRequest request,
            @RequestAttribute(value = AccessControlInterceptor.CURRENT_MEMBER_ID_ATTRIBUTE, required = false) Long currentMemberId,
            @RequestAttribute(value = AccessControlInterceptor.CURRENT_USER_IS_MANAGER_ATTRIBUTE, required = false) Boolean currentUserIsManager) {
        try {
            if (!Boolean.TRUE.equals(currentUserIsManager)) {
                if (currentMemberId == null) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Không xác định được thành viên hiện tại");
                }
                if (request.getProposedById() != null && !Objects.equals(request.getProposedById(), currentMemberId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Bạn không có quyền đề xuất tài liệu cho thành viên khác");
                }
                request.setProposedById(currentMemberId);
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(documentService.create(request));
        } catch (BusinessException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) String reqStatus,
            @RequestParam(required = false) String lookupFolderId,
            @RequestParam(required = false) Integer typeId,
            @RequestParam(required = false) Integer subjectId,
            @RequestParam(required = false) String name) {
        try {
            if (reqStatus != null || lookupFolderId != null || typeId != null || subjectId != null || name != null) {
                return ResponseEntity.ok(documentService.getAll(reqStatus, lookupFolderId, typeId, subjectId, name));
            }
            return ResponseEntity.ok(documentService.getAll());
        } catch (BusinessException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/approve")
    public ResponseEntity<?> approve(@Valid @RequestBody DocumentApprovalRequest request) {
        try {
            return ResponseEntity.ok(documentService.approve(request));
        } catch (BusinessException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/lookup-folder")
    public ResponseEntity<?> moveLookupFolder(
            @PathVariable Long id,
            @Valid @RequestBody DocumentMoveFolderRequest request) {
        try {
            if (request.getDocumentId() != null && !request.getDocumentId().equals(id)) {
                return ResponseEntity.badRequest().body("Document id does not match request body");
            }
            return ResponseEntity.ok(documentService.moveLookupFolder(id, request.getLookupFolderId()));
        } catch (BusinessException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(documentService.getById(id));
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<DocumentResponse>> search(@RequestParam String name) {
        return ResponseEntity.ok(documentService.searchByName(name));
    }

    @GetMapping("/by-subject/{subjectId}")
    public ResponseEntity<List<DocumentResponse>> bySubject(@PathVariable Integer subjectId) {
        return ResponseEntity.ok(documentService.getBySubject(subjectId));
    }

    @GetMapping("/by-type/{typeId}")
    public ResponseEntity<List<DocumentResponse>> byType(@PathVariable Integer typeId) {
        return ResponseEntity.ok(documentService.getByType(typeId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDelete(@PathVariable Long id) {
        documentService.softDeleteById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/hard")
    public ResponseEntity<?> hardDelete(@PathVariable Long id) {
        try {
            documentService.hardDeleteById(id);
            return ResponseEntity.noContent().build();
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
