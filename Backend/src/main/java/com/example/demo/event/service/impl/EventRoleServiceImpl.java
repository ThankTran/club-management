package com.example.demo.event.service.impl;

import com.example.demo.event.dto.request.EventRoleRequest;
import com.example.demo.event.dto.response.EventRoleResponse;
import com.example.demo.event.mapper.EventRoleMapper;
import com.example.demo.event.repository.EventRoleRepository;
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
@CacheConfig(cacheNames = "eventRoles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EventRoleServiceImpl implements com.example.demo.event.service.interfaces.EventRoleService {
    EventRoleRepository eventRoleRepository;
    EventRoleMapper eventRoleMapper;

    @CacheEvict(allEntries = true)
    public EventRoleResponse create(EventRoleRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Event role request must not be empty");
        }
        if (request.getRoleId() == null) {
            throw new IllegalArgumentException("Event role ID must not be empty");
        }
        if (request.getRoleName() == null || request.getRoleName().isBlank()) {
            throw new IllegalArgumentException("Event role name must not be empty");
        }
        if (eventRoleRepository.existsById(request.getRoleId())) {
            throw new IllegalArgumentException("Event role ID already exists: " + request.getRoleId());
        }
        if (eventRoleRepository.findByRoleNameIgnoreCase(request.getRoleName()).isPresent()) {
            throw new IllegalArgumentException("Event role name already exists: " + request.getRoleName());
        }
        return eventRoleMapper.toResponse(eventRoleRepository.save(eventRoleMapper.toEntity(request)));
    }

    @Cacheable(key = "'all'")
    public List<EventRoleResponse> getAll() {
        return eventRoleRepository.findAll().stream().map(eventRoleMapper::toResponse).toList();
    }

    @Cacheable(key = "'name:' + #roleName")
    public EventRoleResponse getByName(String roleName) {
        return eventRoleRepository.findByRoleNameIgnoreCase(roleName).map(eventRoleMapper::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vai trò sự kiện: " + roleName));
    }

    @Async("applicationTaskExecutor")
    public CompletableFuture<List<EventRoleResponse>> getAllAsync() {
        return CompletableFuture.completedFuture(getAll());
    }
}
