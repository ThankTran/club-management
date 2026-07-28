package com.example.demo.event.repository.interfaces;

import com.example.demo.event.entity.EventRole;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRoleRepository extends JpaRepository<EventRole, Short> {
    Optional<EventRole> findByRoleNameIgnoreCase(String roleName);
}
