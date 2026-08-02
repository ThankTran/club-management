package com.example.demo.document.repository;

import com.example.demo.document.entity.DocumentType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentTypeRepository extends JpaRepository<DocumentType, Integer> {
    Optional<DocumentType> findByTypeNameIgnoreCase(String typeName);
}
