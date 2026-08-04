package com.example.demo.event.service.impl;

import com.example.demo.event.dto.request.EventOrganizerRequest;
import com.example.demo.event.dto.response.EventOrganizerResponse;
import com.example.demo.event.mapper.EventOrganizerMapper;
import com.example.demo.event.entity.EventOrganizerId;
import com.example.demo.event.repository.EventOrganizerRepository;
import com.example.demo.event.repository.EventRepository;
import com.example.demo.event.repository.EventRoleRepository;
import com.example.demo.member.repository.MemberRepository;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;

@Service
@Transactional
@CacheConfig(cacheNames = "eventOrganizers")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EventOrganizerServiceImpl implements com.example.demo.event.service.interfaces.EventOrganizerService {
    EventOrganizerRepository eventOrganizerRepository;
    EventRepository eventRepository;
    MemberRepository memberRepository;
    EventRoleRepository eventRoleRepository;
    EventOrganizerMapper eventOrganizerMapper;

    @CacheEvict(allEntries = true)
    public EventOrganizerResponse create(EventOrganizerRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Event organizer request must not be empty");
        }
        boolean exists = eventOrganizerRepository.existsById(new EventOrganizerId(request.getEventId(), request.getMemberId()));
        if (exists) {
            throw new IllegalArgumentException(
                    "Member " + request.getMemberId() + " is already assigned to event " + request.getEventId());
        }

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
        if (eventId == null || eventId.isBlank()) {
            throw new IllegalArgumentException("Event ID must not be empty");
        }
        if (memberId == null) {
            throw new IllegalArgumentException("Member ID must not be empty");
        }
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
