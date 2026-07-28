package com.example.demo.document.controller;

import com.example.demo.document.dto.request.DocumentTypeRequest;
import com.example.demo.document.dto.response.DocumentTypeResponse;
import com.example.demo.document.service.interfaces.DocumentTypeService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/document-types")
public class DocumentTypeController {

    private final DocumentTypeService documentTypeService;

    public DocumentTypeController(DocumentTypeService documentTypeService) {
        this.documentTypeService = documentTypeService;
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody DocumentTypeRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(documentTypeService.create(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<DocumentTypeResponse>> getAll() {
        return ResponseEntity.ok(documentTypeService.getAll());
    }

    @GetMapping("/by-name")
    public ResponseEntity<?> getByName(@RequestParam String name) {
        try {
            return ResponseEntity.ok(documentTypeService.getByName(name));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
