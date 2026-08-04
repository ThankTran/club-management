package com.example.demo.notification.service.impl;

import com.example.demo.notification.service.interfaces.NotificationDispatchService;
import com.example.demo.shared.enums.ApprovalStatusEnum;
import com.example.demo.shared.enums.GraduatedStatusEnum;
import com.example.demo.member.entity.Member;
import com.example.demo.notification.entity.Notification;
import com.example.demo.notification.entity.NotificationRecipient;
import com.example.demo.notification.entity.NotificationRecipientId;
import com.example.demo.member.repository.MemberRepository;
import com.example.demo.notification.repository.NotificationRecipientRepository;
import com.example.demo.notification.repository.NotificationRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationDispatchServiceImpl implements NotificationDispatchService {
    static final int MANAGER_PRIORITY_MAX = 1;
    static final String SEND_METHOD_SYSTEM = "SYSTEM";

    final NotificationRepository notificationRepository;
    final NotificationRecipientRepository notificationRecipientRepository;
    final MemberRepository memberRepository;

    @Override
    @CacheEvict(cacheNames = {"notifications", "notificationRecipients"}, allEntries = true)
    public void toManagers(String title, String content, String targetType, Member sender) {
        dispatch(memberRepository.findManagers(MANAGER_PRIORITY_MAX), title, content, targetType, sender);
    }

    @Override
    @CacheEvict(cacheNames = {"notifications", "notificationRecipients"}, allEntries = true)
    public void toApprovedActiveMembers(String title, String content, String targetType, Member sender) {
        dispatch(memberRepository.findByReqStatus(ApprovalStatusEnum.APPROVED).stream()
                .filter(this::isActiveMember)
                .toList(), title, content, targetType, sender);
    }

    @Override
    @CacheEvict(cacheNames = {"notifications", "notificationRecipients"}, allEntries = true)
    public void toMembers(Collection<Member> members, String title, String content, String targetType, Member sender) {
        dispatch(members, title, content, targetType, sender);
    }

    @Override
    @CacheEvict(cacheNames = {"notifications", "notificationRecipients"}, allEntries = true)
    public void toManagersAndMembers(Collection<Member> members, String title, String content, String targetType, Member sender) {
        List<Member> recipients = new ArrayList<>(members == null ? List.of() : members);
        recipients.addAll(memberRepository.findManagers(MANAGER_PRIORITY_MAX));
        dispatch(recipients, title, content, targetType, sender);
    }

    private void dispatch(Collection<Member> recipients, String title, String content, String targetType, Member sender) {
        Map<Long, Member> uniqueRecipients = uniqueRecipients(recipients);
        if (uniqueRecipients.isEmpty()) {
            return;
        }

        Notification notification = notificationRepository.save(Notification.builder()
                .title(title)
                .content(content)
                .sender(sender)
                .targetType(targetType)
                .sendMethod(SEND_METHOD_SYSTEM)
                .sentAt(LocalDateTime.now())
                .build());

        List<NotificationRecipient> recipientEntities = uniqueRecipients.values().stream()
                .map(member -> NotificationRecipient.builder()
                        .id(new NotificationRecipientId(notification.getNotificationId(), member.getMemberId()))
                        .notification(notification)
                        .member(member)
                        .isRead(Boolean.FALSE)
                        .build())
                .toList();
        notificationRecipientRepository.saveAll(recipientEntities);
    }

    private Map<Long, Member> uniqueRecipients(Collection<Member> recipients) {
        Map<Long, Member> uniqueRecipients = new LinkedHashMap<>();
        if (recipients == null) {
            return uniqueRecipients;
        }
        recipients.stream()
                .filter(member -> member != null && member.getMemberId() != null)
                .forEach(member -> uniqueRecipients.putIfAbsent(member.getMemberId(), member));
        return uniqueRecipients;
    }

    private boolean isActiveMember(Member member) {
        return member != null
                && member.getGraduatedStatus() != GraduatedStatusEnum.GRADUATED
                && member.getGraduatedStatus() != GraduatedStatusEnum.INACTIVE;
    }
}
