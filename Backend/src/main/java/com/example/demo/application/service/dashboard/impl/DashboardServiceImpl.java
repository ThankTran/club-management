package com.example.demo.application.service.dashboard.impl;

import com.example.demo.application.service.dashboard.interfaces.DashboardService;
import com.example.demo.domain.enums.ApprovalStatusEnum;
import com.example.demo.domain.model.document.Document;
import com.example.demo.domain.model.event.Event;
import com.example.demo.domain.model.member.Member;
import com.example.demo.domain.model.notification.Notification;
import com.example.demo.domain.repository.document.DocumentRepository;
import com.example.demo.domain.repository.event.EventRepository;
import com.example.demo.domain.repository.member.MemberRepository;
import com.example.demo.domain.repository.notification.NotificationRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {
    private final MemberRepository memberRepository;
    private final EventRepository eventRepository;
    private final DocumentRepository documentRepository;
    private final NotificationRepository notificationRepository;

    public DashboardServiceImpl(MemberRepository memberRepository,
                                EventRepository eventRepository,
                                DocumentRepository documentRepository,
                                NotificationRepository notificationRepository) {
        this.memberRepository = memberRepository;
        this.eventRepository = eventRepository;
        this.documentRepository = documentRepository;
        this.notificationRepository = notificationRepository;
    }

    @Override
    public Map<String, Object> getOverview() {
        return Map.of(
                "stats", getStats(),
                "activities", getNotifications(),
                "chartData", getChartData());
    }

    @Override
    public List<Map<String, Object>> getStats() {
        List<Member> members = memberRepository.findAll();
        List<Event> events = eventRepository.findAll();
        List<Document> documents = documentRepository.findAll();

        LocalDate today = LocalDate.now();
        long eventsThisMonth = events.stream()
                .filter(event -> event.getEventDate() != null)
                .filter(event -> YearMonth.from(event.getEventDate()).equals(YearMonth.from(today)))
                .count();
        long eventsThisYear = events.stream()
                .filter(event -> event.getEventDate() != null && event.getEventDate().getYear() == today.getYear())
                .count();
        long pendingApprovals = members.stream()
                .filter(member -> member.getReqStatus() == ApprovalStatusEnum.PENDING)
                .count()
                + events.stream()
                .filter(event -> event.getReqStatus() == ApprovalStatusEnum.PENDING)
                .count()
                + documents.stream()
                .filter(document -> document.getReqStatus() == ApprovalStatusEnum.PENDING || document.getReqStatus() == ApprovalStatusEnum.REQUESTED_CHANGES)
                .count();
        return List.of(
                stat("Thành viên", members.size(), "#3b82f6", "#eff6ff", "#bfdbfe", "Tổng trong CLB", null),
                stat("Sự kiện tháng này", eventsThisMonth, "#f59e0b", "#fffbeb", "#fde68a", "Đã lên lịch", null),
                stat("Hoạt động", eventsThisYear, "#10b981", "#f0fdf4", "#a7f3d0", "Tổng trong năm", null),
                stat("Chờ phê duyệt", pendingApprovals, "#ef4444", "#fff5f5", "#fecaca", "Cần xử lý", null));
    }

    @Override
    public List<Map<String, Object>> getNotifications() {
        return notificationRepository.findAll().stream()
                .sorted(Comparator.comparing(
                        Notification::getSentAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(10)
                .map(this::activity)
                .toList();
    }

    private List<Map<String, Object>> getChartData() {
        List<Member> members = memberRepository.findAll();
        List<Event> events = eventRepository.findAll();
        List<Document> documents = documentRepository.findAll();
        YearMonth currentMonth = YearMonth.now();

        return IntStream.rangeClosed(0, 5)
                .mapToObj(offset -> currentMonth.minusMonths(5L - offset))
                .map(month -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("month", "T" + month.getMonthValue());
                    item.put("events", countEventsInMonth(events, month));
                    item.put("docs", countDocumentsInMonth(documents, month));
                    item.put("members", countMembersInMonth(members, month));
                    return item;
                })
                .toList();
    }

    private Map<String, Object> stat(String label,
                                     Object value,
                                     String accent,
                                     String bg,
                                     String border,
                                     String sub,
                                     Integer trend) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("label", label);
        item.put("value", value);
        item.put("accent", accent);
        item.put("bg", bg);
        item.put("border", border);
        item.put("sub", sub);
        if (trend != null) {
            item.put("trend", trend);
        }
        return item;
    }

    private Map<String, Object> activity(Notification notification) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("notificationId", notification.getNotificationId());
        item.put("text", notification.getTitle());
        item.put("time", notification.getSentAt());
        item.put("to", routeForTarget(notification.getTargetType()));
        item.put("read", false);
        return item;
    }

    private String routeForTarget(String targetType) {
        if (targetType == null) {
            return "/dashboard";
        }
        return switch (targetType.toLowerCase()) {
            case "member", "members" -> "/memberadmin";
            case "event", "events" -> "/eventadmin";
            case "document", "documents", "resource", "resources" -> "/resourcesadmin";
            case "finance", "transaction", "transactions" -> "/finance";
            default -> "/dashboard";
        };
    }

    private long countMembersInMonth(List<Member> members, YearMonth month) {
        return members.stream()
                .filter(member -> isSameMonth(member.getCreatedAt(), month))
                .count();
    }

    private long countEventsInMonth(List<Event> events, YearMonth month) {
        return events.stream()
                .filter(event -> event.getEventDate() != null)
                .filter(event -> YearMonth.from(event.getEventDate()).equals(month))
                .count();
    }

    private long countDocumentsInMonth(List<Document> documents, YearMonth month) {
        return documents.stream()
                .filter(document -> isSameMonth(document.getCreatedAt(), month))
                .count();
    }

    private boolean isSameMonth(LocalDateTime dateTime, YearMonth month) {
        return dateTime != null && YearMonth.from(dateTime).equals(month);
    }
}
