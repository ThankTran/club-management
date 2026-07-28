package com.example.demo.notification.domain.service.interfaces;

import com.example.demo.notification.dto.request.NotificationRecipientRequest;

public interface NotificationRecipientDomainService {
    void validateCreateRequest(NotificationRecipientRequest request);

    void validateRecipientUniqueness(Long notificationId, Long memberId, boolean exists);

    void validateDelete(Long notificationId, Long memberId);
}
