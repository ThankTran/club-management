package com.example.demo.notification.mapper;

import com.example.demo.notification.dto.request.NotificationRecipientRequest;
import com.example.demo.notification.dto.response.NotificationRecipientResponse;
import com.example.demo.member.entity.Member;
import com.example.demo.notification.entity.Notification;
import com.example.demo.notification.entity.NotificationRecipient;
import com.example.demo.notification.entity.NotificationRecipientId;
import com.example.demo.shared.config.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfig.class)
public abstract class NotificationRecipientMapper {

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

    @Mapping(source = "id.notificationId", target = "notificationId")
    @Mapping(source = "id.memberId", target = "memberId")
    @Mapping(source = "notification.title", target = "title")
    @Mapping(source = "notification.content", target = "content")
    @Mapping(source = "notification.sender.memberId", target = "senderId")
    @Mapping(source = "notification.targetType", target = "targetType")
    @Mapping(source = "notification.sendMethod", target = "sendMethod")
    @Mapping(source = "notification.sentAt", target = "sentAt")
    public abstract NotificationRecipientResponse toResponse(NotificationRecipient entity);
}

