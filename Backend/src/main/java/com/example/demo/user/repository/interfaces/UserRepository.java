package com.example.demo.user.repository.interfaces;

import com.example.demo.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    @Override
    @EntityGraph(attributePaths = {"member", "member.department", "member.role"})
    List<User> findAll();

    @Override
    @EntityGraph(attributePaths = {"member", "member.department", "member.role"})
    Optional<User> findById(Long userId);

    @EntityGraph(attributePaths = {"member", "member.department", "member.role"})
    Optional<User> findByMemberMemberId(Long memberId);

    @EntityGraph(attributePaths = {"member", "member.department", "member.role"})
    Optional<User> findByMemberStudentId(String studentId);

    boolean existsByMemberMemberId(Long memberId);
}
