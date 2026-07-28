package com.example.demo.notification.domain.service.interfaces;

import com.example.demo.notification.dto.request.NotificationRequest;
import com.example.demo.member.entity.Member;

public interface NotificationDomainService {
    void validateCreateRequest(NotificationRequest request);

    void validateSender(Member sender);
}
