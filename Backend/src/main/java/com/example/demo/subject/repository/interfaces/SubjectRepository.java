package com.example.demo.subject.repository.interfaces;

import com.example.demo.subject.entity.Subject;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubjectRepository extends JpaRepository<Subject, Integer> {
    Optional<Subject> findBySubjectName(String subjectName);

    Optional<Subject> findBySubjectNameIgnoreCase(String subjectName);

    boolean existsBySubjectNameIgnoreCase(String subjectName);
}
