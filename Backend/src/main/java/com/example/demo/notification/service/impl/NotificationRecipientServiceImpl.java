package com.example.demo.notification.service.impl;

import com.example.demo.notification.dto.request.NotificationRecipientRequest;
import com.example.demo.notification.dto.response.NotificationRecipientResponse;
import com.example.demo.notification.mapper.NotificationRecipientMapper;
import com.example.demo.notification.entity.NotificationRecipientId;
import com.example.demo.member.repository.interfaces.MemberRepository;
import com.example.demo.notification.repository.interfaces.NotificationRecipientRepository;
import com.example.demo.notification.repository.interfaces.NotificationRepository;
import com.example.demo.notification.domain.service.interfaces.NotificationRecipientDomainService;
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
@CacheConfig(cacheNames = "notificationRecipients")
public class NotificationRecipientServiceImpl implements com.example.demo.notification.service.interfaces.NotificationRecipientService {
    private final NotificationRecipientRepository notificationRecipientRepository;
    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;
    private final NotificationRecipientMapper notificationRecipientMapper;
    private final NotificationRecipientDomainService notificationRecipientDomainService;

    public NotificationRecipientServiceImpl(
            NotificationRecipientRepository notificationRecipientRepository,
            NotificationRepository notificationRepository,
            MemberRepository memberRepository,
            NotificationRecipientMapper notificationRecipientMapper,
            NotificationRecipientDomainService notificationRecipientDomainService) {
        this.notificationRecipientRepository = notificationRecipientRepository;
        this.notificationRepository = notificationRepository;
        this.memberRepository = memberRepository;
        this.notificationRecipientMapper = notificationRecipientMapper;
        this.notificationRecipientDomainService = notificationRecipientDomainService;
    }

    @CacheEvict(allEntries = true)
    public NotificationRecipientResponse create(NotificationRecipientRequest request) {
        notificationRecipientDomainService.validateCreateRequest(request);
        notificationRecipientDomainService.validateRecipientUniqueness(
                request.getNotificationId(),
                request.getMemberId(),
                notificationRecipientRepository.existsById(
                        new NotificationRecipientId(request.getNotificationId(), request.getMemberId())));

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
        notificationRecipientDomainService.validateDelete(notificationId, memberId);
        notificationRecipientRepository.deleteById(new NotificationRecipientId(notificationId, memberId));
    }

    @Async("applicationTaskExecutor")
    public CompletableFuture<List<NotificationRecipientResponse>> getByMemberAsync(Long memberId) {
        return CompletableFuture.completedFuture(getByMember(memberId));
    }
}
