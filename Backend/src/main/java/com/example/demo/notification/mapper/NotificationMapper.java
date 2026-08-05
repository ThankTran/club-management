package com.example.demo.notification.mapper;

import com.example.demo.notification.dto.request.NotificationRequest;
import com.example.demo.notification.dto.response.NotificationResponse;
import com.example.demo.member.entity.Member;
import com.example.demo.notification.entity.Notification;
import com.example.demo.shared.config.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfig.class)
public interface NotificationMapper {

    @Mapping(target = "sender", source = "sender")
    Notification toEntity(NotificationRequest request, Member sender);

    @Mapping(source = "sender.memberId", target = "senderId")
    NotificationResponse toResponse(Notification entity);
}

