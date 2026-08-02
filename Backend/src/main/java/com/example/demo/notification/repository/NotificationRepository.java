package com.example.demo.notification.repository;

import com.example.demo.notification.entity.Notification;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    @Override
    @EntityGraph(attributePaths = {"sender"})
    List<Notification> findAll();

    @Override
    @EntityGraph(attributePaths = {"sender"})
    Optional<Notification> findById(Long notificationId);

    @EntityGraph(attributePaths = {"sender"})
    @Query("""
            SELECT n
            FROM Notification n
            WHERE LOWER(n.title) LIKE LOWER(CONCAT('%', :title, '%'))
            ORDER BY n.sentAt DESC
            """)
    List<Notification> searchByTitle(@Param("title") String title);

    @EntityGraph(attributePaths = {"sender"})
    @Query("""
            SELECT n
            FROM Notification n
            WHERE LOWER(n.targetType) = LOWER(:targetType)
            ORDER BY n.sentAt DESC
            """)
    List<Notification> findByTargetType(@Param("targetType") String targetType);

    boolean existsBySenderMemberId(Long memberId);
}
