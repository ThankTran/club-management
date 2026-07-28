package com.example.demo.notification.service.impl;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo.notification.service.impl.NotificationDispatchServiceImpl;
import com.example.demo.shared.enums.ApprovalStatusEnum;
import com.example.demo.member.entity.Member;
import com.example.demo.notification.entity.Notification;
import com.example.demo.member.repository.interfaces.MemberRepository;
import com.example.demo.notification.repository.interfaces.NotificationRecipientRepository;
import com.example.demo.notification.repository.interfaces.NotificationRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class NotificationDispatchServiceImplTest {
    @Test
    void toManagersUsesTargetedQueryInsteadOfLoadingAllMembers() {
        NotificationRepository notificationRepository = Mockito.mock(NotificationRepository.class);
        NotificationRecipientRepository notificationRecipientRepository = Mockito.mock(NotificationRecipientRepository.class);
        MemberRepository memberRepository = Mockito.mock(MemberRepository.class);

        NotificationDispatchServiceImpl service = new NotificationDispatchServiceImpl(
                notificationRepository,
                notificationRecipientRepository,
                memberRepository);

        Member manager = Member.builder().memberId(1L).fullName("Manager").build();
        when(memberRepository.findManagers(1)).thenReturn(List.of(manager));
        when(notificationRepository.save(Mockito.any(Notification.class)))
                .thenAnswer(invocation -> {
                    Notification notification = invocation.getArgument(0);
                    notification.setNotificationId(100L);
                    notification.setSentAt(LocalDateTime.now());
                    return notification;
                });

        service.toManagers("Title", "Content", "FINANCE", null);

        verify(memberRepository).findManagers(1);
        verify(memberRepository, never()).findAll();
    }

    @Test
    void toManagersAndMembersUsesTargetedManagerQuery() {
        NotificationRepository notificationRepository = Mockito.mock(NotificationRepository.class);
        NotificationRecipientRepository notificationRecipientRepository = Mockito.mock(NotificationRecipientRepository.class);
        MemberRepository memberRepository = Mockito.mock(MemberRepository.class);

        NotificationDispatchServiceImpl service = new NotificationDispatchServiceImpl(
                notificationRepository,
                notificationRecipientRepository,
                memberRepository);

        Member manager = Member.builder().memberId(1L).fullName("Manager").build();
        Member member = Member.builder().memberId(2L).fullName("Member").build();
        when(memberRepository.findManagers(1)).thenReturn(List.of(manager));
        when(notificationRepository.save(Mockito.any(Notification.class)))
                .thenAnswer(invocation -> {
                    Notification notification = invocation.getArgument(0);
                    notification.setNotificationId(100L);
                    notification.setSentAt(LocalDateTime.now());
                    return notification;
                });

        service.toManagersAndMembers(List.of(member), "Title", "Content", "FINANCE", null);

        verify(memberRepository).findManagers(1);
        verify(memberRepository, never()).findAll();
    }

    @Test
    void toApprovedActiveMembersStillUsesApprovedMembersQuery() {
        NotificationRepository notificationRepository = Mockito.mock(NotificationRepository.class);
        NotificationRecipientRepository notificationRecipientRepository = Mockito.mock(NotificationRecipientRepository.class);
        MemberRepository memberRepository = Mockito.mock(MemberRepository.class);

        NotificationDispatchServiceImpl service = new NotificationDispatchServiceImpl(
                notificationRepository,
                notificationRecipientRepository,
                memberRepository);

        when(memberRepository.findByReqStatus(ApprovalStatusEnum.APPROVED)).thenReturn(List.of());
        when(notificationRepository.save(Mockito.any(Notification.class)))
                .thenAnswer(invocation -> {
                    Notification notification = invocation.getArgument(0);
                    notification.setNotificationId(100L);
                    notification.setSentAt(LocalDateTime.now());
                    return notification;
                });

        service.toApprovedActiveMembers("Title", "Content", "FINANCE", null);

        verify(memberRepository).findByReqStatus(ApprovalStatusEnum.APPROVED);
    }
}
