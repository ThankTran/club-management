package com.example.demo.document.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.example.demo.document.repository.DocumentFileRepository;
import com.example.demo.document.repository.DocumentRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never"
})
@Import(DocumentTimestampPersistenceTest.CacheTestConfiguration.class)
class DocumentTimestampPersistenceTest {
    @Autowired
    private EntityManager entityManager;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentFileRepository documentFileRepository;

    @Test
    void preservesExplicitCreatedAtWhenPersistingSeededDocument() {
        LocalDateTime seededCreatedAt = LocalDateTime.of(2025, 11, 19, 10, 7);
        Document document = Document.builder()
                .documentName("Seeded architecture notes")
                .createdAt(seededCreatedAt)
                .updatedAt(seededCreatedAt.plusDays(2))
                .build();

        entityManager.persist(document);
        entityManager.flush();
        entityManager.clear();

        Document savedDocument = entityManager.find(Document.class, document.getDocumentId());

        assertEquals(seededCreatedAt, savedDocument.getCreatedAt());
    }

    @Test
    void assignsCreatedAtWhenPersistingNewDocumentWithoutExplicitTimestamp() {
        Document document = Document.builder()
                .documentName("Runtime document")
                .build();

        entityManager.persist(document);
        entityManager.flush();
        entityManager.clear();

        Document savedDocument = entityManager.find(Document.class, document.getDocumentId());

        assertNotNull(savedDocument.getCreatedAt());
    }

    @Test
    void bulkUpdatesSeededDocumentAndFileTimeline() {
        LocalDateTime originalCreatedAt = LocalDateTime.of(2026, 6, 2, 11, 0);
        LocalDateTime normalizedCreatedAt = LocalDateTime.of(2025, 10, 16, 12, 21);
        Document document = Document.builder()
                .documentName("Document needing timeline normalization")
                .createdAt(originalCreatedAt)
                .updatedAt(originalCreatedAt)
                .build();
        entityManager.persist(document);
        DocumentFile file = DocumentFile.builder()
                .document(document)
                .fileUrl("/uploads/documents/original.pdf")
                .fileName("original.pdf")
                .fileSize(1000L)
                .mimeType("application/pdf")
                .uploadedAt(originalCreatedAt)
                .build();
        entityManager.persist(file);
        entityManager.flush();

        documentRepository.updateSeedTimeline(
                document.getDocumentId(),
                normalizedCreatedAt,
                normalizedCreatedAt.plusDays(2),
                normalizedCreatedAt.plusHours(4));
        documentFileRepository.updateSeedTimeline(
                file.getFileId(),
                normalizedCreatedAt.plusHours(1),
                "/uploads/documents/normalized.pdf");
        entityManager.clear();

        Document normalizedDocument = entityManager.find(Document.class, document.getDocumentId());
        DocumentFile normalizedFile = entityManager.find(DocumentFile.class, file.getFileId());

        assertEquals(normalizedCreatedAt, normalizedDocument.getCreatedAt());
        assertEquals(normalizedCreatedAt.plusDays(2), normalizedDocument.getUpdatedAt());
        assertEquals(normalizedCreatedAt.plusHours(4), normalizedDocument.getApprovedAt());
        assertEquals(normalizedCreatedAt.plusHours(1), normalizedFile.getUploadedAt());
        assertEquals("/uploads/documents/normalized.pdf", normalizedFile.getFileUrl());
    }

    @TestConfiguration
    static class CacheTestConfiguration {
        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager();
        }
    }
}
