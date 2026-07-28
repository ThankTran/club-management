package com.example.demo.notification.service.interfaces;

import com.example.demo.notification.dto.request.NotificationRecipientRequest;
import com.example.demo.notification.dto.response.NotificationRecipientResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface NotificationRecipientService {
    NotificationRecipientResponse create(NotificationRecipientRequest request);

    List<NotificationRecipientResponse> getByNotification(Long notificationId);

    List<NotificationRecipientResponse> getByMember(Long memberId);

    NotificationRecipientResponse markAsRead(Long notificationId, Long memberId);

    void delete(Long notificationId, Long memberId);

    CompletableFuture<List<NotificationRecipientResponse>> getByMemberAsync(Long memberId);
}
