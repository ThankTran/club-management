package com.example.demo.notification.mapper;

import com.example.demo.notification.dto.request.NotificationRequest;
import com.example.demo.notification.dto.response.NotificationResponse;
import com.example.demo.member.entity.Member;
import com.example.demo.notification.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public Notification toEntity(NotificationRequest request, Member sender) {
        return Notification.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .sender(sender)
                .targetType(request.getTargetType())
                .sendMethod(request.getSendMethod())
                .build();
    }

    public NotificationResponse toResponse(Notification entity) {
        Long senderId = entity.getSender() == null ? null : entity.getSender().getMemberId();
        return NotificationResponse.builder()
                .notificationId(entity.getNotificationId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .senderId(senderId)
                .targetType(entity.getTargetType())
                .sendMethod(entity.getSendMethod())
                .sentAt(entity.getSentAt())
                .build();
    }
}
