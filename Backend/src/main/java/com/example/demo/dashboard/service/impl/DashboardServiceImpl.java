package com.example.demo.dashboard.service.impl;

import com.example.demo.dashboard.service.interfaces.DashboardService;
import com.example.demo.shared.enums.ApprovalStatusEnum;
import com.example.demo.document.entity.Document;
import com.example.demo.event.entity.Event;
import com.example.demo.member.entity.Member;
import com.example.demo.notification.entity.Notification;
import com.example.demo.document.repository.interfaces.DocumentRepository;
import com.example.demo.event.repository.interfaces.EventRepository;
import com.example.demo.member.repository.interfaces.MemberRepository;
import com.example.demo.notification.repository.interfaces.NotificationRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
        return getOverview(null, null);
    }

    @Override
    public Map<String, Object> getOverview(LocalDate fromDate, LocalDate toDate) {
        DateRange range = resolveDateRange(fromDate, toDate);
        return Map.of(
                "stats", getStats(),
                "activities", getNotifications(),
                "chartData", getChartData(range.startDate(), range.endDate()));
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

    private List<Map<String, Object>> getChartData(LocalDate startDate, LocalDate endDate) {
        List<Member> members = memberRepository.findAll();
        List<Event> events = eventRepository.findAll();
        List<Document> documents = documentRepository.findAll();
        List<YearMonth> months = buildMonthRange(startDate, endDate);

        return months.stream()
                .map(month -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("month", month.getMonth().getDisplayName(TextStyle.SHORT, new Locale("vi", "VN")));
                    item.put("events", countEventsInMonth(events, month, startDate, endDate));
                    item.put("docs", countDocumentsInMonth(documents, month, startDate, endDate));
                    item.put("members", countMembersInMonth(members, month, startDate, endDate));
                    return item;
                })
                .toList();
    }

    private DateRange resolveDateRange(LocalDate fromDate, LocalDate toDate) {
        YearMonth currentMonth = YearMonth.now();
        LocalDate defaultStart = currentMonth.minusMonths(5).atDay(1);
        LocalDate defaultEnd = LocalDate.now();

        LocalDate start = fromDate != null ? fromDate : defaultStart;
        LocalDate end = toDate != null ? toDate : defaultEnd;

        if (start.isAfter(end)) {
            LocalDate temp = start;
            start = end;
            end = temp;
        }

        return new DateRange(start, end);
    }

    private List<YearMonth> buildMonthRange(LocalDate startDate, LocalDate endDate) {
        YearMonth startMonth = YearMonth.from(startDate);
        YearMonth endMonth = YearMonth.from(endDate);
        List<YearMonth> months = new ArrayList<>();
        YearMonth cursor = startMonth;

        while (!cursor.isAfter(endMonth)) {
            months.add(cursor);
            cursor = cursor.plusMonths(1);
        }

        return months;
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

    private long countMembersInMonth(List<Member> members, YearMonth month, LocalDate startDate, LocalDate endDate) {
        return members.stream()
                .filter(member -> isSameMonth(member.getCreatedAt(), month))
                .filter(member -> isInRange(member.getCreatedAt(), startDate, endDate))
                .count();
    }

    private long countEventsInMonth(List<Event> events, YearMonth month, LocalDate startDate, LocalDate endDate) {
        return events.stream()
                .filter(event -> event.getEventDate() != null)
                .filter(event -> YearMonth.from(event.getEventDate()).equals(month))
                .filter(event -> isInRange(event.getEventDate(), startDate, endDate))
                .count();
    }

    private long countDocumentsInMonth(List<Document> documents, YearMonth month, LocalDate startDate, LocalDate endDate) {
        return documents.stream()
                .filter(document -> isSameMonth(document.getCreatedAt(), month))
                .filter(document -> isInRange(document.getCreatedAt(), startDate, endDate))
                .count();
    }

    private boolean isSameMonth(LocalDateTime dateTime, YearMonth month) {
        return dateTime != null && YearMonth.from(dateTime).equals(month);
    }

    private boolean isInRange(LocalDateTime dateTime, LocalDate startDate, LocalDate endDate) {
        return dateTime != null && isInRange(dateTime.toLocalDate(), startDate, endDate);
    }

    private boolean isInRange(LocalDate date, LocalDate startDate, LocalDate endDate) {
        return date != null && !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    private record DateRange(LocalDate startDate, LocalDate endDate) {
    }
}
