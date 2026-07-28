package com.example.demo.notification.repository.interfaces;

import com.example.demo.notification.entity.NotificationRecipient;
import com.example.demo.notification.entity.NotificationRecipientId;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRecipientRepository extends JpaRepository<NotificationRecipient, NotificationRecipientId> {
    @EntityGraph(attributePaths = {"notification", "notification.sender", "member"})
    List<NotificationRecipient> findByNotificationNotificationId(Long notificationId);

    @EntityGraph(attributePaths = {"notification", "notification.sender", "member"})
    List<NotificationRecipient> findByMemberMemberId(Long memberId);

    boolean existsByNotificationNotificationId(Long notificationId);

    boolean existsByMemberMemberId(Long memberId);
}
