package com.example.demo.notification.mapper;

import com.example.demo.notification.dto.request.NotificationRecipientRequest;
import com.example.demo.notification.dto.response.NotificationRecipientResponse;
import com.example.demo.member.entity.Member;
import com.example.demo.notification.entity.Notification;
import com.example.demo.notification.entity.NotificationRecipient;
import com.example.demo.notification.entity.NotificationRecipientId;
import org.springframework.stereotype.Component;

@Component
public class NotificationRecipientMapper {

    public NotificationRecipient toEntity(
            NotificationRecipientRequest request,
            Notification notification,
            Member member) {
        NotificationRecipientId id = new NotificationRecipientId(request.getNotificationId(), request.getMemberId());
        return NotificationRecipient.builder()
                .id(id)
                .notification(notification)
                .member(member)
                .isRead(request.getIsRead() != null ? request.getIsRead() : Boolean.FALSE)
                .readAt(request.getReadAt())
                .build();
    }

    public NotificationRecipientResponse toResponse(NotificationRecipient entity) {
        Long notificationId = entity.getId() != null ? entity.getId().getNotificationId() : null;
        Long memberId = entity.getId() != null ? entity.getId().getMemberId() : null;
        Notification notification = entity.getNotification();
        Member sender = notification == null ? null : notification.getSender();

        return NotificationRecipientResponse.builder()
                .notificationId(notificationId)
                .memberId(memberId)
                .isRead(entity.getIsRead())
                .readAt(entity.getReadAt())
                .title(notification == null ? null : notification.getTitle())
                .content(notification == null ? null : notification.getContent())
                .senderId(sender == null ? null : sender.getMemberId())
                .targetType(notification == null ? null : notification.getTargetType())
                .sendMethod(notification == null ? null : notification.getSendMethod())
                .sentAt(notification == null ? null : notification.getSentAt())
                .build();
    }
}
