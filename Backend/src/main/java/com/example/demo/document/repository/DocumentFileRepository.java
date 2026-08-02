package com.example.demo.document.repository;

import com.example.demo.document.entity.DocumentFile;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DocumentFileRepository extends JpaRepository<DocumentFile, Long> {
    List<DocumentFile> findByDocumentDocumentId(Long documentId);

    Optional<DocumentFile> findFirstByDocumentDocumentIdOrderByUploadedAtAsc(Long documentId);

    Optional<DocumentFile> findFirstByDocumentDocumentIdOrderByUploadedAtDesc(Long documentId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update DocumentFile f
            set f.uploadedAt = :uploadedAt,
                f.fileUrl = :fileUrl
            where f.fileId = :id
            """)
    void updateSeedTimeline(
            @Param("id") Long id,
            @Param("uploadedAt") LocalDateTime uploadedAt,
            @Param("fileUrl") String fileUrl);

    boolean existsByDocumentDocumentId(Long documentId);
}
