package com.example.demo.event.service.impl;

import com.example.demo.event.dto.request.EventEvaluationRequest;
import com.example.demo.event.dto.request.EventRequest;
import com.example.demo.event.dto.response.EventCalendarLinkResponse;
import com.example.demo.event.dto.response.EventEvaluationResponse;
import com.example.demo.event.dto.response.EventPublicResponse;
import com.example.demo.event.dto.response.EventResponse;
import com.example.demo.event.mapper.EventMapper;
import com.example.demo.notification.service.interfaces.NotificationDispatchService;
import com.example.demo.shared.enums.ApprovalStatusEnum;
import com.example.demo.shared.enums.EventStatusEnum;
import com.example.demo.event.entity.Event;
import com.example.demo.member.entity.Member;
import com.example.demo.event.repository.EventOrganizerRepository;
import com.example.demo.event.repository.EventRegistrationRepository;
import com.example.demo.event.repository.EventRepository;
import com.example.demo.finance.repository.TransactionRepository;
import com.example.demo.member.repository.MemberRepository;


import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@CacheConfig(cacheNames = "events")
public class EventServiceImpl implements com.example.demo.event.service.interfaces.EventService {
    private static final ZoneId EVENT_TIMEZONE = ZoneId.of("Asia/Bangkok");
    private static final DateTimeFormatter GOOGLE_CALENDAR_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");
    private static final String TARGET_EVENT = "EVENT";

    private final EventRepository eventRepository;
    private final EventOrganizerRepository eventOrganizerRepository;
    private final EventRegistrationRepository eventRegistrationRepository;
    private final TransactionRepository transactionRepository;
    private final MemberRepository memberRepository;
    private final EventMapper eventMapper;
    private final NotificationDispatchService notificationDispatchService;

    public EventServiceImpl(
            EventRepository eventRepository,
            EventOrganizerRepository eventOrganizerRepository,
            EventRegistrationRepository eventRegistrationRepository,
            TransactionRepository transactionRepository,
            MemberRepository memberRepository,
            EventMapper eventMapper,
            NotificationDispatchService notificationDispatchService) {
        this.eventRepository = eventRepository;
        this.eventOrganizerRepository = eventOrganizerRepository;
        this.eventRegistrationRepository = eventRegistrationRepository;
        this.transactionRepository = transactionRepository;
        this.memberRepository = memberRepository;
        this.eventMapper = eventMapper;
        this.notificationDispatchService = notificationDispatchService;
    }

    @CacheEvict(allEntries = true)
    public EventResponse create(EventRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Event request must not be empty");
        }
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        if (request.getEvaluationDate() != null && !request.getEvaluationDate().isAfter(request.getEndTime())) {
            throw new IllegalArgumentException("Evaluation date must be after event end time");
        }
        
        if (eventRepository.existsById(request.getEventId())) {
            throw new IllegalArgumentException("Event ID already exists: " + request.getEventId());
        }
        if (eventRepository.existsByEventNameIgnoreCase(request.getEventName())) {
            throw new IllegalArgumentException("Event name already exists: " + request.getEventName());
        }

        Member evaluatedBy = null;
        if (request.getEvaluatedById() != null) {
            evaluatedBy = memberRepository.findById(request.getEvaluatedById())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Không tìm thấy thành viên đánh giá: " + request.getEvaluatedById()));
        }
        Event savedEvent = eventRepository.save(eventMapper.toEntity(request, evaluatedBy));
        notificationDispatchService.toApprovedActiveMembers(
                "Sự kiện mới",
                "Sự kiện " + savedEvent.getEventName() + " vừa được tạo.",
                TARGET_EVENT,
                evaluatedBy);
        return toResponse(savedEvent);
    }

    @CacheEvict(allEntries = true)
    public EventResponse update(String id, EventRequest request) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Event ID must not be empty");
        }
        if (request == null) {
            throw new IllegalArgumentException("Event request must not be empty");
        }
        if (request.getEventId() != null && !request.getEventId().isBlank()
                && !id.equals(request.getEventId())) {
            throw new IllegalArgumentException("Event ID in path and body must match");
        }
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sự kiện: " + id));
        eventRepository.findByEventNameIgnoreCase(request.getEventName())
                .filter(existing -> !existing.getEventId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Event name already exists: " + request.getEventName());
                });

        event.setEventName(request.getEventName());
        event.setLocation(request.getLocation());
        event.setEventDate(request.getEventDate());
        event.setStartTime(request.getStartTime());
        event.setEndTime(request.getEndTime());
        event.setEstimatedCost(request.getEstimatedCost());
        event.setCapacity(request.getCapacity());
        event.setOrganizer(request.getOrganizer());
        event.setTag(request.getTag());
        if (request.getStatus() != null) {
            if (request.getStatus() == EventStatusEnum.Cancelled
                    && event.getStatus() != null
                    && event.getStatus() != EventStatusEnum.NotStarted) {
                throw new IllegalArgumentException("Chỉ được hủy khi và chỉ khi sự kiện chưa hoạt động.");
            }
            event.setStatus(request.getStatus());
        }
        if (request.getReqStatus() != null) {
            event.setReqStatus(request.getReqStatus());
        }
        event.setDescription(request.getDescription());

        return toResponse(eventRepository.save(event));
    }

    @Cacheable(key = "'all'")
    public List<EventResponse> getAll() {
        return eventRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Cacheable(key = "'public-upcoming'")
    public List<EventPublicResponse> getPublicUpcomingEvents() {
        LocalDate today = LocalDate.now(EVENT_TIMEZONE);
        return eventRepository.findAll().stream()
                .filter(event -> event.getReqStatus() == ApprovalStatusEnum.APPROVED)
                .filter(this::isPublicUpcomingEvent)
                .filter(event -> event.getEventDate() == null || !event.getEventDate().isBefore(today))
                .sorted(Comparator.comparing(
                        Event::getEventDate,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .map(this::toPublicResponse)
                .toList();
    }

    @Cacheable(key = "'name:' + #eventName")
    public List<EventResponse> searchByName(String eventName) {
        return eventRepository.searchByName(eventName).stream()
                .map(this::toResponse)
                .toList();
    }

    @Cacheable(key = "'range:' + #from + '|' + #to")
    public List<EventResponse> getByDateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Date range must not be empty");
        }
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("End date must be on or after start date");
        }
        return eventRepository.findByEventDateRange(from, to).stream()
                .map(this::toResponse)
                .toList();
    }

    @Cacheable(key = "'id:' + #id")
    public EventResponse getById(String id) {
        return eventRepository.findById(id).map(this::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sự kiện: " + id));
    }

    @Override
    public EventCalendarLinkResponse getGoogleCalendarLink(String id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sự kiện: " + id));

        if (event.getStartTime() == null || event.getEndTime() == null) {
            throw new IllegalArgumentException("Sự kiện chưa có đủ thông tin thời gian để tạo link Google Calendar");
        }

        String details = buildEventDetails(event);

        String dates = event.getStartTime().atZone(EVENT_TIMEZONE).toInstant()
                .atOffset(ZoneOffset.UTC)
                .format(GOOGLE_CALENDAR_DATE_FORMAT)
                + "/"
                + event.getEndTime().atZone(EVENT_TIMEZONE).toInstant()
                .atOffset(ZoneOffset.UTC)
                .format(GOOGLE_CALENDAR_DATE_FORMAT);

        String googleCalendarLink = UriComponentsBuilder
                .fromUriString("https://calendar.google.com/calendar/render")
                .queryParam("action", "TEMPLATE")
                .queryParam("text", event.getEventName())
                .queryParam("dates", dates)
                .queryParam("details", details)
                .queryParam("location", event.getLocation())
                .build()
                .toUriString();

        return EventCalendarLinkResponse.builder()
                .eventId(event.getEventId())
                .googleCalendarLink(googleCalendarLink)
                .build();
    }

    @Override
    @CacheEvict(allEntries = true)
    public EventEvaluationResponse createOrUpdateEvaluation(EventEvaluationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Evaluation request must not be empty");
        }
        if (request.getEventId() == null || request.getEventId().isBlank()) {
            throw new IllegalArgumentException("Event ID must not be empty");
        }
        if (request.getEvaluationContent() == null || request.getEvaluationContent().isBlank()) {
            throw new IllegalArgumentException("Evaluation content must not be empty");
        }

        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sự kiện: " + request.getEventId()));
        LocalDateTime evaluationDate = request.getEvaluationDate() == null
                ? LocalDateTime.now()
                : request.getEvaluationDate();
        if (event.getEndTime() != null && !evaluationDate.isAfter(event.getEndTime())) {
            throw new IllegalArgumentException("Evaluation date must be after event end time");
        }

        Member evaluatedBy = null;
        if (request.getEvaluatedById() != null) {
            evaluatedBy = memberRepository.findById(request.getEvaluatedById())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Không tìm thấy thành viên đánh giá: " + request.getEvaluatedById()));
        }

        event.setEvaluatedBy(evaluatedBy);
        event.setEvaluationDate(evaluationDate);
        event.setEvaluationContent(request.getEvaluationContent());
        Event savedEvent = eventRepository.save(event);
        notificationDispatchService.toManagersAndMembers(
                eventRegistrationRepository.findByEventEventId(savedEvent.getEventId()).stream()
                        .map(registration -> registration.getMember())
                        .toList(),
                "Đánh giá sự kiện đã hoàn tất",
                "Sự kiện " + savedEvent.getEventName() + " đã có nội dung đánh giá.",
                TARGET_EVENT,
                evaluatedBy);
        return toEvaluationResponse(savedEvent);
    }

    @Override
    public EventEvaluationResponse getEvaluationByEvent(String eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sự kiện: " + eventId));
        return toEvaluationResponse(event);
    }

    @CacheEvict(allEntries = true)
    public void delete(String id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sự kiện: " + id));
        if (event.getStatus() == EventStatusEnum.InProgress) {
            throw new IllegalArgumentException("Chỉ được hủy khi và chỉ khi sự kiện chưa hoạt động.");
        }
        if (eventOrganizerRepository.existsByEventEventId(id)) {
            throw new IllegalArgumentException(
                    "Cannot delete event because organizers still reference it.");
        }
        if (eventRegistrationRepository.existsByEventEventId(id)) {
            throw new IllegalArgumentException(
                    "Cannot delete event because registrations still reference it.");
        }
        if (transactionRepository.existsByEventEventId(id)) {
            throw new IllegalArgumentException(
                    "Cannot delete event because transactions still reference it.");
        }
        event.setDeletedAt(LocalDateTime.now());
        eventRepository.save(event);
        notificationDispatchService.toApprovedActiveMembers(
                "Sự kiện đã được xóa",
                "Sự kiện " + event.getEventName() + " đã được xóa khỏi hệ thống.",
                TARGET_EVENT,
                null);
    }

    @Async("applicationTaskExecutor")
    public CompletableFuture<List<EventResponse>> getAllAsync() {
        return CompletableFuture.completedFuture(getAll());
    }

    @Async("applicationTaskExecutor")
    public CompletableFuture<List<EventResponse>> searchByNameAsync(String eventName) {
        return CompletableFuture.completedFuture(searchByName(eventName));
    }

    @Async("applicationTaskExecutor")
    public CompletableFuture<List<EventResponse>> getByDateRangeAsync(LocalDate from, LocalDate to) {
        return CompletableFuture.completedFuture(getByDateRange(from, to));
    }

    @Async("applicationTaskExecutor")
    public CompletableFuture<EventResponse> getByIdAsync(String id) {
        return CompletableFuture.completedFuture(getById(id));
    }

    private String buildEventDetails(Event event) {
        StringBuilder details = new StringBuilder();
        if (event.getDescription() != null && !event.getDescription().isBlank()) {
            details.append(event.getDescription().trim());
        }
        if (event.getEventId() != null && !event.getEventId().isBlank()) {
            if (details.length() > 0) {
                details.append("\n\n");
            }
            details.append("Mã sự kiện: ").append(event.getEventId());
        }
        return details.toString();
    }

    private EventResponse toResponse(Event event) {
        EventResponse response = eventMapper.toResponse(event);
        response.setAttendance(eventRegistrationRepository.countByEventEventId(event.getEventId()));
        return response;
    }

    private boolean isPublicUpcomingEvent(Event event) {
        return event.getStatus() == EventStatusEnum.NotStarted || event.getStatus() == EventStatusEnum.InProgress;
    }

    private EventPublicResponse toPublicResponse(Event event) {
        return EventPublicResponse.builder()
                .eventId(event.getEventId())
                .eventName(event.getEventName())
                .eventDate(event.getEventDate())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .location(event.getLocation())
                .description(event.getDescription())
                .tag(event.getTag())
                .build();
    }

    private EventEvaluationResponse toEvaluationResponse(Event event) {
        Long evaluatedById = event.getEvaluatedBy() == null ? null : event.getEvaluatedBy().getMemberId();
        return EventEvaluationResponse.builder()
                .eventId(event.getEventId())
                .eventName(event.getEventName())
                .evaluatedById(evaluatedById)
                .evaluationDate(event.getEvaluationDate())
                .evaluationContent(event.getEvaluationContent())
                .build();
    }
}
