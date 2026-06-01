package com.example.demo.application.service.event.impl;

import com.example.demo.application.dto.request.event.EventAttendanceRequest;
import com.example.demo.application.dto.response.event.EventRegistrationResponse;
import com.example.demo.application.mapper.event.EventRegistrationMapper;
import com.example.demo.application.service.notification.interfaces.NotificationDispatchService;
import com.example.demo.domain.enums.ApprovalStatusEnum;
import com.example.demo.domain.enums.EventStatusEnum;
import com.example.demo.domain.enums.TransactionStatus;
import com.example.demo.domain.enums.TransactionType;
import com.example.demo.domain.model.event.EventRegistration;
import com.example.demo.domain.model.event.EventRegistrationId;
import com.example.demo.domain.model.finance.Transaction;
import com.example.demo.domain.repository.event.EventRegistrationRepository;
import com.example.demo.domain.repository.event.EventRepository;
import com.example.demo.domain.repository.finance.TransactionRepository;
import com.example.demo.domain.repository.member.MemberRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@CacheConfig(cacheNames = "eventRegistrations")
public class EventRegistrationServiceImpl implements com.example.demo.application.service.event.interfaces.EventRegistrationService {
    private static final String TARGET_EVENT = "EVENT";
    private static final String TARGET_FINANCE = "FINANCE";

    private final EventRegistrationRepository eventRegistrationRepository;
    private final EventRepository eventRepository;
    private final MemberRepository memberRepository;
    private final TransactionRepository transactionRepository;
    private final EventRegistrationMapper eventRegistrationMapper;
    private final NotificationDispatchService notificationDispatchService;

    public EventRegistrationServiceImpl(
            EventRegistrationRepository eventRegistrationRepository,
            EventRepository eventRepository,
            MemberRepository memberRepository,
            TransactionRepository transactionRepository,
            EventRegistrationMapper eventRegistrationMapper,
            NotificationDispatchService notificationDispatchService) {
        this.eventRegistrationRepository = eventRegistrationRepository;
        this.eventRepository = eventRepository;
        this.memberRepository = memberRepository;
        this.transactionRepository = transactionRepository;
        this.eventRegistrationMapper = eventRegistrationMapper;
        this.notificationDispatchService = notificationDispatchService;
    }

    @Override
    @CacheEvict(cacheNames = {"eventRegistrations", "events", "transactions", "finance"}, allEntries = true)
    public EventRegistrationResponse register(String eventId, Long memberId) {
        if (eventId == null || eventId.isBlank()) {
            throw new IllegalArgumentException("Event ID must not be empty");
        }
        if (memberId == null) {
            throw new IllegalArgumentException("Member ID must not be empty");
        }

        var event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sự kiện: " + eventId));
        if (event.getStatus() == EventStatusEnum.Finished || event.getStatus() == EventStatusEnum.Cancelled) {
            throw new IllegalArgumentException("Cannot register for a finished or cancelled event");
        }

        var member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thành viên: " + memberId));
        if (member.getReqStatus() != ApprovalStatusEnum.APPROVED) {
            throw new IllegalArgumentException("Only approved members can register for events");
        }

        EventRegistrationId id = new EventRegistrationId(eventId, memberId);
        if (eventRegistrationRepository.existsById(id)) {
            throw new IllegalArgumentException("Member is already registered for this event");
        }
        if (event.getCapacity() != null
                && event.getCapacity() > 0
                && eventRegistrationRepository.countByEventEventId(eventId) >= event.getCapacity()) {
            throw new IllegalArgumentException("Event registration is full");
        }

        EventRegistration registration = EventRegistration.builder()
                .id(id)
                .event(event)
                .member(member)
                .build();
        EventRegistration saved = eventRegistrationRepository.save(registration);
        createEventDueIfNeeded(event, member);
        notificationDispatchService.toMembers(
                List.of(member),
                "Đăng ký sự kiện thành công",
                "Bạn đã đăng ký tham gia sự kiện " + event.getEventName() + ".",
                TARGET_EVENT,
                member);
        return eventRegistrationMapper.toResponse(saved);
    }

    @Override
    @Cacheable(key = "'event:' + #eventId")
    public List<EventRegistrationResponse> getByEvent(String eventId) {
        return eventRegistrationRepository.findByEventEventId(eventId).stream()
                .map(eventRegistrationMapper::toResponse)
                .toList();
    }

    @Override
    @Cacheable(key = "'member:' + #memberId")
    public List<EventRegistrationResponse> getByMember(Long memberId) {
        return eventRegistrationRepository.findByMemberMemberId(memberId).stream()
                .map(eventRegistrationMapper::toResponse)
                .toList();
    }

    @Override
    @CacheEvict(cacheNames = {"eventRegistrations", "events"}, allEntries = true)
    public List<EventRegistrationResponse> updateAttendance(String eventId, EventAttendanceRequest request) {
        if (eventId == null || eventId.isBlank()) {
            throw new IllegalArgumentException("Event ID must not be empty");
        }
        if (!eventRepository.existsById(eventId)) {
            throw new IllegalArgumentException("Không tìm thấy sự kiện: " + eventId);
        }
        Set<Long> memberIds = resolveMemberIds(request);
        boolean attended = request == null || request.getAttended() == null || request.getAttended();

        for (Long memberId : memberIds) {
            EventRegistration registration = eventRegistrationRepository
                    .findById(new EventRegistrationId(eventId, memberId))
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Không tìm thấy đăng ký cho sự kiện " + eventId + " và thành viên " + memberId));
            registration.setAttended(attended);
            registration.setAttendedAt(attended ? LocalDateTime.now() : null);
            eventRegistrationRepository.save(registration);
        }
        return getByEvent(eventId);
    }

    @Override
    @CacheEvict(cacheNames = {"eventRegistrations", "events", "transactions", "finance"}, allEntries = true)
    public void unregister(String eventId, Long memberId) {
        if (eventId == null || eventId.isBlank()) {
            throw new IllegalArgumentException("Event ID must not be empty");
        }
        if (memberId == null) {
            throw new IllegalArgumentException("Member ID must not be empty");
        }
        EventRegistrationId id = new EventRegistrationId(eventId, memberId);
        if (!eventRegistrationRepository.existsById(id)) {
            throw new IllegalArgumentException("Không tìm thấy đăng ký sự kiện");
        }
        var registration = eventRegistrationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đăng ký sự kiện"));
        var event = registration.getEvent();
        var member = registration.getMember();
        eventRegistrationRepository.deleteById(id);
        cancelPendingEventDues(eventId, memberId);
        notificationDispatchService.toMembers(
                List.of(member),
                "Đã hủy đăng ký sự kiện",
                "Bạn đã hủy đăng ký sự kiện " + event.getEventName() + ".",
                TARGET_EVENT,
                member);
    }

    private void createEventDueIfNeeded(com.example.demo.domain.model.event.Event event, com.example.demo.domain.model.member.Member member) {
        BigDecimal amount = event.getEstimatedCost();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        String transactionId = buildEventDueId(event.getEventId(), member.getMemberId());
        Transaction due = Transaction.builder()
                .transactionId(transactionId)
                .event(event)
                .member(member)
                .counterpartyName(member.getFullName())
                .type(TransactionType.INCOME)
                .amount(amount)
                .description("Phí tham gia sự kiện: " + event.getEventName())
                .transactionDate(LocalDateTime.now())
                .status(TransactionStatus.PENDING)
                .build();
        transactionRepository.save(due);
        notificationDispatchService.toMembers(
                List.of(member),
                "Phí tham gia sự kiện",
                "Bạn có khoản phí tham gia sự kiện " + event.getEventName() + ".",
                TARGET_FINANCE,
                null);
    }

    private void cancelPendingEventDues(String eventId, Long memberId) {
        transactionRepository
                .findActiveByMemberIdAndEventIdAndTypeAndStatus(
                        memberId, eventId, TransactionType.INCOME, TransactionStatus.PENDING)
                .forEach(transaction -> {
                    transaction.setStatus(TransactionStatus.CANCELLED);
                    transaction.setDeletedAt(LocalDateTime.now());
                    transactionRepository.save(transaction);
                });
    }

    private String buildEventDueId(String eventId, Long memberId) {
        String suffix = String.valueOf(System.currentTimeMillis() % 1_000_000L);
        String prefix = "DUE-EVENT-";
        String raw = prefix + eventId + "-" + memberId + "-" + suffix;
        return raw.length() <= 50 ? raw : prefix + Math.abs((eventId + "-" + memberId).hashCode()) + "-" + suffix;
    }

    private Set<Long> resolveMemberIds(EventAttendanceRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Attendance request must not be empty");
        }
        Set<Long> memberIds = new LinkedHashSet<>();
        if (request.getMemberIds() != null) {
            memberIds.addAll(request.getMemberIds());
        }
        if (request.getMemberId() != null) {
            memberIds.add(request.getMemberId());
        }
        memberIds.remove(null);
        if (memberIds.isEmpty()) {
            throw new IllegalArgumentException("Member ID must not be empty");
        }
        return memberIds;
    }
}
