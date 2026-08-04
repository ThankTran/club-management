package com.example.demo.notification.service.impl;

import com.example.demo.notification.dto.request.NotificationRequest;
import com.example.demo.notification.dto.response.NotificationResponse;
import com.example.demo.notification.mapper.NotificationMapper;
import com.example.demo.member.entity.Member;
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
@CacheConfig(cacheNames = "notifications")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationServiceImpl implements com.example.demo.notification.service.interfaces.NotificationService {
    NotificationRepository notificationRepository;
    NotificationRecipientRepository notificationRecipientRepository;
    MemberRepository memberRepository;
    NotificationMapper notificationMapper;

    @CacheEvict(allEntries = true)
    public NotificationResponse create(NotificationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Notification request must not be empty");
        }

        Member sender = null;
        if (request.getSenderId() != null) {
            sender = memberRepository.findById(request.getSenderId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Không tìm thấy người gửi: " + request.getSenderId()));
            if (sender.getRole() == null) {
                throw new IllegalArgumentException("Sender role is missing");
            }
        }

        var entity = notificationMapper.toEntity(request, sender);
        entity.setSentAt(LocalDateTime.now());
        return notificationMapper.toResponse(notificationRepository.save(entity));
    }

    @Cacheable(key = "'all'")
    public List<NotificationResponse> getAll() {
        return notificationRepository.findAll().stream().map(notificationMapper::toResponse).toList();
    }

    @Cacheable(key = "'title:' + #title")
    public List<NotificationResponse> searchByTitle(String title) {
        return notificationRepository.searchByTitle(title).stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    @Cacheable(key = "'target:' + #targetType")
    public List<NotificationResponse> getByTargetType(String targetType) {
        return notificationRepository.findByTargetType(targetType).stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    @Cacheable(key = "'id:' + #id")
    public NotificationResponse getById(Long id) {
        return notificationRepository.findById(id).map(notificationMapper::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông báo: " + id));
    }

    @CacheEvict(allEntries = true)
    public void delete(Long id) {
        if (!notificationRepository.existsById(id)) {
            throw new IllegalArgumentException("Notification not found: " + id);
        }
        if (notificationRecipientRepository.existsByNotificationNotificationId(id)) {
            throw new IllegalArgumentException(
                    "Cannot delete notification because recipients still reference it.");
        }
        notificationRepository.deleteById(id);
    }

    @Async("applicationTaskExecutor")
    public CompletableFuture<List<NotificationResponse>> getAllAsync() {
        return CompletableFuture.completedFuture(getAll());
    }
}
