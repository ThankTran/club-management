package com.example.demo.notification.service.interfaces;

import com.example.demo.member.entity.Member;
import java.util.Collection;

public interface NotificationDispatchService {
    void toManagers(String title, String content, String targetType, Member sender);

    void toApprovedActiveMembers(String title, String content, String targetType, Member sender);

    void toMembers(Collection<Member> members, String title, String content, String targetType, Member sender);

    void toManagersAndMembers(Collection<Member> members, String title, String content, String targetType, Member sender);
}
