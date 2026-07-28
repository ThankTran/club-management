package com.example.demo.notification.domain.service.impl;

import com.example.demo.notification.domain.service.interfaces.NotificationDomainService;
import com.example.demo.notification.dto.request.NotificationRequest;
import com.example.demo.member.entity.Member;
import org.springframework.stereotype.Service;

@Service
public class NotificationDomainServiceImpl implements NotificationDomainService {
    @Override
    public void validateCreateRequest(NotificationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Notification request must not be empty");
        }
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new IllegalArgumentException("Notification title must not be empty");
        }
        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new IllegalArgumentException("Notification content must not be empty");
        }
        if (request.getTargetType() == null || request.getTargetType().isBlank()) {
            throw new IllegalArgumentException("Notification target type must not be empty");
        }
        if (request.getSendMethod() == null || request.getSendMethod().isBlank()) {
            throw new IllegalArgumentException("Notification send method must not be empty");
        }
    }

    @Override
    public void validateSender(Member sender) {
        if (sender == null) {
            throw new IllegalArgumentException("Sender must exist");
        }
        if (sender.getRole() == null) {
            throw new IllegalArgumentException("Sender role is missing");
        }
    }
}
