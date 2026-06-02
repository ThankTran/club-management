package com.example.demo.domain.repository.document;

import com.example.demo.domain.model.document.Document;
import com.example.demo.domain.enums.ApprovalStatusEnum;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    @Override
    @EntityGraph(attributePaths = {"type", "subject", "proposedBy", "approvedBy"})
    List<Document> findAll();

    @Override
    @EntityGraph(attributePaths = {"type", "subject", "proposedBy", "approvedBy"})
    Optional<Document> findById(Long documentId);

    @EntityGraph(attributePaths = {"type", "subject", "proposedBy", "approvedBy"})
    @Query("""
            SELECT d
            FROM Document d
            WHERE (:reqStatus IS NULL OR d.reqStatus = :reqStatus)
              AND (:lookupFolderId IS NULL OR d.lookupFolderId = :lookupFolderId)
              AND (:typeId IS NULL OR d.type.typeId = :typeId)
              AND (:subjectId IS NULL OR d.subject.subjectId = :subjectId)
              AND (:name IS NULL OR LOWER(d.documentName) LIKE LOWER(CONCAT('%', :name, '%')))
            ORDER BY d.createdAt DESC
            """)
    List<Document> findWithFilters(
            @Param("reqStatus") ApprovalStatusEnum reqStatus,
            @Param("lookupFolderId") String lookupFolderId,
            @Param("typeId") Integer typeId,
            @Param("subjectId") Integer subjectId,
            @Param("name") String name);

    @EntityGraph(attributePaths = {"type", "subject", "proposedBy", "approvedBy"})
    @Query("""
            SELECT d
            FROM Document d
            WHERE LOWER(d.documentName) LIKE LOWER(CONCAT('%', :documentName, '%'))
            ORDER BY d.createdAt DESC
            """)
    List<Document> searchByName(@Param("documentName") String documentName);

    @EntityGraph(attributePaths = {"type", "subject", "proposedBy", "approvedBy"})
    @Query("""
            SELECT d
            FROM Document d
            WHERE d.subject.subjectId = :subjectId
            ORDER BY d.createdAt DESC
            """)
    List<Document> findBySubjectId(@Param("subjectId") Integer subjectId);

    @EntityGraph(attributePaths = {"type", "subject", "proposedBy", "approvedBy"})
    @Query("""
            SELECT d
            FROM Document d
            WHERE d.type.typeId = :typeId
            ORDER BY d.createdAt DESC
            """)
    List<Document> findByTypeId(@Param("typeId") Integer typeId);

    @Modifying
    @Query("update Document d set d.deletedAt = current_timestamp where d.documentId = :id")
    void softDeleteById(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Document d
            set d.createdAt = :createdAt,
                d.updatedAt = :updatedAt,
                d.approvedAt = :approvedAt
            where d.documentId = :id
            """)
    void updateSeedTimeline(
            @Param("id") Long id,
            @Param("createdAt") LocalDateTime createdAt,
            @Param("updatedAt") LocalDateTime updatedAt,
            @Param("approvedAt") LocalDateTime approvedAt);

    boolean existsByDocumentNameIgnoreCaseAndTypeTypeIdAndSubjectSubjectId(String documentName, Integer typeId, Integer subjectId);

    boolean existsBySubjectSubjectId(Integer subjectId);

    boolean existsByTypeTypeId(Integer typeId);

    boolean existsByProposedByMemberId(Long memberId);

    boolean existsByApprovedByMemberId(Long memberId);
}
