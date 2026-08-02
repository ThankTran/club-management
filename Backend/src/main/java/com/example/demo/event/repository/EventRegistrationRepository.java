package com.example.demo.event.repository;

import com.example.demo.event.entity.EventRegistration;
import com.example.demo.event.entity.EventRegistrationId;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRegistrationRepository extends JpaRepository<EventRegistration, EventRegistrationId> {
    @EntityGraph(attributePaths = {"event", "member", "member.department", "member.role"})
    List<EventRegistration> findByEventEventId(String eventId);

    @EntityGraph(attributePaths = {"event", "member", "member.department", "member.role"})
    List<EventRegistration> findByMemberMemberId(Long memberId);

    boolean existsByEventEventId(String eventId);

    long countByEventEventId(String eventId);
}
