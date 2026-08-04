package com.example.demo.notification.service.impl;

import com.example.demo.notification.dto.request.NotificationRecipientRequest;
import com.example.demo.notification.dto.response.NotificationRecipientResponse;
import com.example.demo.notification.mapper.NotificationRecipientMapper;
import com.example.demo.notification.entity.NotificationRecipientId;
import com.example.demo.member.repository.MemberRepository;
import com.example.demo.notification.repository.NotificationRecipientRepository;
import com.example.demo.notification.repository.NotificationRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;

@Service
@Transactional
@CacheConfig(cacheNames = "notificationRecipients")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationRecipientServiceImpl implements com.example.demo.notification.service.interfaces.NotificationRecipientService {
    NotificationRecipientRepository notificationRecipientRepository;
    NotificationRepository notificationRepository;
    MemberRepository memberRepository;
    NotificationRecipientMapper notificationRecipientMapper;

    @CacheEvict(allEntries = true)
    public NotificationRecipientResponse create(NotificationRecipientRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Notification recipient request must not be empty");
        }
        if (request.getNotificationId() == null) {
            throw new IllegalArgumentException("Notification ID must not be empty");
        }
        if (request.getMemberId() == null) {
            throw new IllegalArgumentException("Member ID must not be empty");
        }
        boolean exists = notificationRecipientRepository.existsById(
                new NotificationRecipientId(request.getNotificationId(), request.getMemberId()));
        if (exists) {
            throw new IllegalArgumentException(
                    "Member " + request.getMemberId() + " is already a recipient of notification " + request.getNotificationId());
        }

        var notification = notificationRepository.findById(request.getNotificationId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy thông báo: " + request.getNotificationId()));
        var member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thành viên: " + request.getMemberId()));
        var entity = notificationRecipientMapper.toEntity(request, notification, member);
        return notificationRecipientMapper.toResponse(notificationRecipientRepository.save(entity));
    }

    @Cacheable(key = "'notification:' + #notificationId")
    public List<NotificationRecipientResponse> getByNotification(Long notificationId) {
        return notificationRecipientRepository.findByNotificationNotificationId(notificationId).stream()
                .map(notificationRecipientMapper::toResponse)
                .toList();
    }

    @Cacheable(key = "'member:' + #memberId")
    public List<NotificationRecipientResponse> getByMember(Long memberId) {
        return notificationRecipientRepository.findByMemberMemberId(memberId).stream()
                .map(notificationRecipientMapper::toResponse)
                .toList();
    }

    @CacheEvict(allEntries = true)
    public NotificationRecipientResponse markAsRead(Long notificationId, Long memberId) {
        var id = new NotificationRecipientId(notificationId, memberId);
        var recipient = notificationRecipientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy người nhận thông báo: " + notificationId + "/" + memberId));

        recipient.setIsRead(true);
        recipient.setReadAt(LocalDateTime.now());
        return notificationRecipientMapper.toResponse(notificationRecipientRepository.save(recipient));
    }

    @CacheEvict(allEntries = true)
    public void delete(Long notificationId, Long memberId) {
        if (notificationId == null) {
            throw new IllegalArgumentException("Notification ID must not be empty");
        }
        if (memberId == null) {
            throw new IllegalArgumentException("Member ID must not be empty");
        }
        notificationRecipientRepository.deleteById(new NotificationRecipientId(notificationId, memberId));
    }

    @Async("applicationTaskExecutor")
    public CompletableFuture<List<NotificationRecipientResponse>> getByMemberAsync(Long memberId) {
        return CompletableFuture.completedFuture(getByMember(memberId));
    }
}
