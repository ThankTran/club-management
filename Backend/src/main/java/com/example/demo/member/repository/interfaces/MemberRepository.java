package com.example.demo.member.repository.interfaces;

import com.example.demo.member.entity.Member;
import com.example.demo.shared.enums.ApprovalStatusEnum;
import com.example.demo.shared.enums.GraduatedStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    @Override
    @EntityGraph(attributePaths = {"department", "role", "approver"})
    List<Member> findAll();

    @Override
    @EntityGraph(attributePaths = {"department", "role", "approver"})
    Optional<Member> findById(Long memberId);

    Optional<Member> findByStudentId(String studentId);

    Optional<Member> findByEmail(String email);

    @EntityGraph(attributePaths = {"department", "role"})
    List<Member> findAllByFullNameContainingIgnoreCase(String fullName);

    @EntityGraph(attributePaths = {"department", "role"})
    List<Member> findAllByDepartmentDepartmentId(Long departmentId);

    @EntityGraph(attributePaths = {"department", "role", "approver"})
    List<Member> findByReqStatus(ApprovalStatusEnum reqStatus);

    @EntityGraph(attributePaths = {"department", "role", "approver"})
    List<Member> findByGraduatedStatus(GraduatedStatusEnum graduatedStatus);

    @EntityGraph(attributePaths = {"department", "role", "approver"})
    @Query("""
            SELECT m
            FROM Member m
            LEFT JOIN m.role r
            WHERE m.deletedAt IS NULL
              AND r.priority IS NOT NULL
              AND r.priority <= :maxPriority
            """)
    List<Member> findManagers(@Param("maxPriority") Integer maxPriority);

    @EntityGraph(attributePaths = {"department", "role"})
    List<Member> findAllByOrderByFullNameAsc();

    @EntityGraph(attributePaths = {"department", "role"})
    List<Member> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"department", "role", "approver"})
    @Query("""
            SELECT m
            FROM Member m
            LEFT JOIN m.department d
            LEFT JOIN m.role r
            WHERE (:studentId IS NULL OR LOWER(m.studentId) LIKE LOWER(CONCAT('%', :studentId, '%')))
              AND (:fullName IS NULL OR LOWER(m.fullName) LIKE LOWER(CONCAT('%', :fullName, '%')))
              AND (:departmentId IS NULL OR d.departmentId = :departmentId)
              AND (:reqStatus IS NULL OR m.reqStatus = :reqStatus)
              AND (:graduatedStatus IS NULL OR m.graduatedStatus = :graduatedStatus)
              AND (
                    :rolePriority IS NULL
                    OR (:rolePriorityGreaterThan = TRUE AND r.priority > :rolePriority)
                    OR ((:rolePriorityGreaterThan IS NULL OR :rolePriorityGreaterThan = FALSE) AND r.priority = :rolePriority)
                  )
            """)
    Page<Member> searchMembers(@Param("studentId") String studentId,
                               @Param("fullName") String fullName,
                               @Param("departmentId") Long departmentId,
                               @Param("reqStatus") ApprovalStatusEnum reqStatus,
                               @Param("graduatedStatus") GraduatedStatusEnum graduatedStatus,
                               @Param("rolePriority") Integer rolePriority,
                               @Param("rolePriorityGreaterThan") Boolean rolePriorityGreaterThan,
                               Pageable pageable);

    boolean existsByStudentId(String studentId);

    boolean existsByEmail(String email);

    boolean existsByDepartmentDepartmentId(Long departmentId);

    boolean existsByRoleRoleId(Long roleId);

    boolean existsByApproverMemberId(Long memberId);
}
