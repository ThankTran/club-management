package com.example.demo.event.repository;

import com.example.demo.event.entity.Event;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventRepository extends JpaRepository<Event, String> {
    @Override
    @EntityGraph(attributePaths = {"evaluatedBy"})
    List<Event> findAll();

    @Override
    @EntityGraph(attributePaths = {"evaluatedBy"})
    Optional<Event> findById(String eventId);

    @EntityGraph(attributePaths = {"evaluatedBy"})
    @Query("""
            SELECT e
            FROM Event e
            WHERE LOWER(e.eventName) LIKE LOWER(CONCAT('%', :eventName, '%'))
            ORDER BY e.eventDate DESC
            """)
    List<Event> searchByName(@Param("eventName") String eventName);

    @EntityGraph(attributePaths = {"evaluatedBy"})
    @Query("""
            SELECT e
            FROM Event e
            WHERE e.eventDate BETWEEN :from AND :to
            ORDER BY e.eventDate DESC
            """)
    List<Event> findByEventDateRange(@Param("from") LocalDate from, @Param("to") LocalDate to);

    Optional<Event> findByEventNameIgnoreCase(String eventName);

    boolean existsByEventNameIgnoreCase(String eventName);

    boolean existsByEvaluatedByMemberId(Long memberId);
}
