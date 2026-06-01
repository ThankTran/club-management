package com.example.demo.application.service.document.impl;

import com.example.demo.application.dto.request.document.DocumentFileRequest;
import com.example.demo.application.dto.response.document.DocumentFileResponse;
import com.example.demo.application.mapper.document.DocumentFileMapper;
import com.example.demo.application.service.document.interfaces.DocumentFileService;
import com.example.demo.domain.model.document.Document;
import com.example.demo.domain.repository.document.DocumentFileRepository;
import com.example.demo.domain.repository.document.DocumentRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@CacheConfig(cacheNames = "documentFiles")
public class DocumentFileServiceImpl implements DocumentFileService {
    private final DocumentFileRepository documentFileRepository;
    private final DocumentRepository documentRepository;
    private final DocumentFileMapper documentFileMapper;
    private final String uploadDir = "uploads/documents";

    public DocumentFileServiceImpl(
            DocumentFileRepository documentFileRepository,
            DocumentRepository documentRepository,
            DocumentFileMapper documentFileMapper) {
        this.documentFileRepository = documentFileRepository;
        this.documentRepository = documentRepository;
        this.documentFileMapper = documentFileMapper;
    }

    @Override
    @CacheEvict(cacheNames = {"documentFiles", "documents"}, allEntries = true, beforeInvocation = true)
    public DocumentFileResponse create(DocumentFileRequest request) {
        Document document = documentRepository.findById(request.getDocumentId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài liệu: " + request.getDocumentId()));
        String url = resolveUrl(request);
        return documentFileMapper.toResponse(
                documentFileRepository.save(documentFileMapper.toEntity(request, document,url)));
    }

    @Override
    @Cacheable(key = "'document:' + #documentId")
    public List<DocumentFileResponse> getByDocumentId(Long documentId) {
        return documentFileRepository.findByDocumentDocumentId(documentId).stream()
                .map(documentFileMapper::toResponse)
                .toList();
    }

    @Override
    @CacheEvict(cacheNames = {"documentFiles", "documents"}, allEntries = true, beforeInvocation = true)
    public void delete(Long fileId) {
        documentFileRepository.deleteById(fileId);
    }

    @Override
    @Async("applicationTaskExecutor")
    public CompletableFuture<List<DocumentFileResponse>> getByDocumentIdAsync(Long documentId) {
        return CompletableFuture.completedFuture(getByDocumentId(documentId));
    }

    private String save(MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("File is empty");
            }

            // Tạo folder nếu chưa có
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Lấy tên file gốc
            String originalFileName = file.getOriginalFilename();

            // Lấy đuôi file: .png, .jpg, .pdf...
            String extension = "";

            if (originalFileName != null && originalFileName.contains(".")) {
                extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }

            // Tạo tên file mới để tránh trùng
            String storedFileName = UUID.randomUUID() + extension;

            // Đường dẫn lưu file thật
            Path filePath = uploadPath.resolve(storedFileName);

            // Lưu file vào folder
            Files.copy(
                    file.getInputStream(),
                    filePath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            // URL/path để lưu vào database
            return "/uploads/documents/" + storedFileName;

        } catch (IOException e) {
            throw new RuntimeException("Save file failed", e);
        }
    }

    private String resolveUrl(DocumentFileRequest request) {
        MultipartFile file = request.getFile();
        if (file != null && !file.isEmpty()) {
            return save(file);
        }

        String fileUrl = normalizeBlank(request.getFileUrl());
        if (fileUrl == null) {
            throw new IllegalArgumentException("File or file URL is required");
        }
        String lowerUrl = fileUrl.toLowerCase(Locale.ROOT);
        if (!lowerUrl.startsWith("http://") && !lowerUrl.startsWith("https://")) {
            throw new IllegalArgumentException("File URL must start with http:// or https://");
        }

        if (normalizeBlank(request.getFileName()) == null) {
            request.setFileName(fileNameFromUrl(fileUrl));
        }
        if (request.getFileSize() == null || request.getFileSize() <= 0) {
            request.setFileSize(0L);
        }
        if (normalizeBlank(request.getMimeType()) == null) {
            request.setMimeType(mimeTypeFromFileName(request.getFileName()));
        }
        return fileUrl;
    }

    private String normalizeBlank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String fileNameFromUrl(String url) {
        String withoutQuery = url.split("\\?", 2)[0];
        int slashIndex = withoutQuery.lastIndexOf('/');
        String fileName = slashIndex >= 0 ? withoutQuery.substring(slashIndex + 1) : withoutQuery;
        return fileName.isBlank() ? "linked-document" : fileName;
    }

    private String mimeTypeFromFileName(String fileName) {
        String normalized = fileName == null ? "" : fileName.toLowerCase(Locale.ROOT);
        if (normalized.endsWith(".pdf")) return "application/pdf";
        if (normalized.endsWith(".ppt") || normalized.endsWith(".pptx")) return "application/vnd.ms-powerpoint";
        if (normalized.endsWith(".doc") || normalized.endsWith(".docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        if (normalized.endsWith(".xls") || normalized.endsWith(".xlsx")) return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        if (normalized.endsWith(".zip")) return "application/zip";
        return "application/octet-stream";
    }
}

