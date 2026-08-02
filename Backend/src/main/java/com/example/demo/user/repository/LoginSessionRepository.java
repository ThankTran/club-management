package com.example.demo.user.repository;

import com.example.demo.user.entity.LoginSession;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginSessionRepository extends JpaRepository<LoginSession, Long> {
    @EntityGraph(attributePaths = {"user", "user.member"})
    List<LoginSession> findTop20ByUserUserIdOrderByLoginAtDesc(Long userId);
}
