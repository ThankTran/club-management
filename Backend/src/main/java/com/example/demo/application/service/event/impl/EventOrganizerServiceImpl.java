package com.example.demo.application.service.event.impl;

import com.example.demo.application.dto.request.event.EventOrganizerRequest;
import com.example.demo.application.dto.response.event.EventOrganizerResponse;
import com.example.demo.application.mapper.event.EventOrganizerMapper;
import com.example.demo.domain.model.event.EventOrganizerId;
import com.example.demo.domain.repository.event.EventOrganizerRepository;
import com.example.demo.domain.repository.event.EventRepository;
import com.example.demo.domain.repository.event.EventRoleRepository;
import com.example.demo.domain.repository.member.MemberRepository;
import com.example.demo.domain.service.event.EventOrganizerDomainService;
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
@CacheConfig(cacheNames = "eventOrganizers")
public class EventOrganizerServiceImpl implements com.example.demo.application.service.event.interfaces.EventOrganizerService {
    private final EventOrganizerRepository eventOrganizerRepository;
    private final EventRepository eventRepository;
    private final MemberRepository memberRepository;
    private final EventRoleRepository eventRoleRepository;
    private final EventOrganizerMapper eventOrganizerMapper;
    private final EventOrganizerDomainService eventOrganizerDomainService;

    public EventOrganizerServiceImpl(
            EventOrganizerRepository eventOrganizerRepository,
            EventRepository eventRepository,
            MemberRepository memberRepository,
            EventRoleRepository eventRoleRepository,
            EventOrganizerMapper eventOrganizerMapper,
            EventOrganizerDomainService eventOrganizerDomainService) {
        this.eventOrganizerRepository = eventOrganizerRepository;
        this.eventRepository = eventRepository;
        this.memberRepository = memberRepository;
        this.eventRoleRepository = eventRoleRepository;
        this.eventOrganizerMapper = eventOrganizerMapper;
        this.eventOrganizerDomainService = eventOrganizerDomainService;
    }

    @CacheEvict(allEntries = true)
    public EventOrganizerResponse create(EventOrganizerRequest request) {
        eventOrganizerDomainService.validateCreateRequest(request);
        eventOrganizerDomainService.validateAssignmentUniqueness(
                request.getEventId(),
                request.getMemberId(),
                eventOrganizerRepository.existsById(new EventOrganizerId(request.getEventId(), request.getMemberId())));

        var event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sự kiện: " + request.getEventId()));
        var member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thành viên: " + request.getMemberId()));
        var role = eventRoleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vai trò sự kiện: " + request.getRoleId()));
        var entity = eventOrganizerMapper.toEntity(request, event, member, role);
        return eventOrganizerMapper.toResponse(eventOrganizerRepository.save(entity));
    }

    @Cacheable(key = "'event:' + #eventId")
    public List<EventOrganizerResponse> getByEvent(String eventId) {
        return eventOrganizerRepository.findByEventEventId(eventId).stream()
                .map(eventOrganizerMapper::toResponse)
                .toList();
    }

    @Cacheable(key = "'member:' + #memberId")
    public List<EventOrganizerResponse> getByMember(Long memberId) {
        return eventOrganizerRepository.findByMemberMemberId(memberId).stream()
                .map(eventOrganizerMapper::toResponse)
                .toList();
    }

    @CacheEvict(allEntries = true)
    public void delete(String eventId, Long memberId) {
        eventOrganizerDomainService.validateDelete(eventId, memberId);
        EventOrganizerId id = new EventOrganizerId(eventId, memberId);
        if (!eventOrganizerRepository.existsById(id)) {
            throw new IllegalArgumentException(
                    "Không tìm thấy phân công ban tổ chức cho sự kiện " + eventId + " và thành viên " + memberId);
        }
        eventOrganizerRepository.deleteById(id);
    }

    @Async("applicationTaskExecutor")
    public CompletableFuture<List<EventOrganizerResponse>> getByEventAsync(String eventId) {
        return CompletableFuture.completedFuture(getByEvent(eventId));
    }
}
