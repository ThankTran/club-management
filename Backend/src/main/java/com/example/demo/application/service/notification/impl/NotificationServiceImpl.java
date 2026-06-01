package com.example.demo.application.service.notification.impl;

import com.example.demo.application.dto.request.notification.NotificationRequest;
import com.example.demo.application.dto.response.notification.NotificationResponse;
import com.example.demo.application.mapper.notification.NotificationMapper;
import com.example.demo.domain.model.member.Member;
import com.example.demo.domain.repository.member.MemberRepository;
import com.example.demo.domain.repository.notification.NotificationRecipientRepository;
import com.example.demo.domain.repository.notification.NotificationRepository;
import com.example.demo.domain.service.notification.NotificationDomainService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@CacheConfig(cacheNames = "notifications")
public class NotificationServiceImpl implements com.example.demo.application.service.notification.interfaces.NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationRecipientRepository notificationRecipientRepository;
    private final MemberRepository memberRepository;
    private final NotificationMapper notificationMapper;
    private final NotificationDomainService notificationDomainService;

    public NotificationServiceImpl(
            NotificationRepository notificationRepository,
            NotificationRecipientRepository notificationRecipientRepository,
            MemberRepository memberRepository,
            NotificationMapper notificationMapper,
            NotificationDomainService notificationDomainService) {
        this.notificationRepository = notificationRepository;
        this.notificationRecipientRepository = notificationRecipientRepository;
        this.memberRepository = memberRepository;
        this.notificationMapper = notificationMapper;
        this.notificationDomainService = notificationDomainService;
    }

    @CacheEvict(allEntries = true)
    public NotificationResponse create(NotificationRequest request) {
        notificationDomainService.validateCreateRequest(request);

        Member sender = null;
        if (request.getSenderId() != null) {
            sender = memberRepository.findById(request.getSenderId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Không tìm thấy người gửi: " + request.getSenderId()));
            notificationDomainService.validateSender(sender);
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
