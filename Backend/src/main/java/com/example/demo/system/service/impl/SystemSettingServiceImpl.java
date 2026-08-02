package com.example.demo.system.service.impl;

import com.example.demo.system.dto.request.SystemSettingRequest;
import com.example.demo.system.dto.response.SystemSettingResponse;
import com.example.demo.system.mapper.SystemSettingMapper;
import com.example.demo.member.entity.Member;
import com.example.demo.member.repository.MemberRepository;
import com.example.demo.system.repository.SystemSettingRepository;

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
@CacheConfig(cacheNames = "systemSettings")
public class SystemSettingServiceImpl implements com.example.demo.system.service.interfaces.SystemSettingService {
    private final SystemSettingRepository systemSettingRepository;
    private final MemberRepository memberRepository;
    private final SystemSettingMapper systemSettingMapper;

    public SystemSettingServiceImpl(
            SystemSettingRepository systemSettingRepository,
            MemberRepository memberRepository,
            SystemSettingMapper systemSettingMapper) {
        this.systemSettingRepository = systemSettingRepository;
        this.memberRepository = memberRepository;
        this.systemSettingMapper = systemSettingMapper;
    }

    @CacheEvict(allEntries = true)
    public SystemSettingResponse createOrUpdate(SystemSettingRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("System setting request must not be empty");
        }
        Member updatedBy = null;
        if (request.getUpdatedById() != null) {
            updatedBy = memberRepository.findById(request.getUpdatedById())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thành viên cập nhật: " + request.getUpdatedById()));
            if (updatedBy == null) {
                throw new IllegalArgumentException("Updated by member must exist");
            }
        }
        return systemSettingMapper.toResponse(
                systemSettingRepository.save(systemSettingMapper.toEntity(request, updatedBy)));
    }

    @Cacheable(key = "'all'")
    public List<SystemSettingResponse> getAll() {
        return systemSettingRepository.findAll().stream().map(systemSettingMapper::toResponse).toList();
    }

    @Cacheable(key = "'search:' + #key")
    public List<SystemSettingResponse> searchByKey(String key) {
        return systemSettingRepository.findBySettingKeyContainingIgnoreCaseOrderBySettingKeyAsc(key)
                .stream().map(systemSettingMapper::toResponse).toList();
    }

    @Cacheable(key = "'key:' + #key")
    public SystemSettingResponse getByKey(String key) {
        return systemSettingRepository.findById(key).map(systemSettingMapper::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy cài đặt: " + key));
    }

    @Cacheable(key = "'keyOrDefault:' + #key + ':' + #defaultValue")
    public SystemSettingResponse getByKeyOrDefault(String key, String defaultValue, String description) {
        return systemSettingRepository.findById(key).map(systemSettingMapper::toResponse)
                .orElseGet(() -> SystemSettingResponse.builder()
                        .settingKey(key)
                        .settingValue(defaultValue)
                        .description(description)
                        .updatedById(null)
                        .updatedAt(null)
                        .build());
    }

    @CacheEvict(allEntries = true)
    public void delete(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Setting key must not be empty");
        }
        systemSettingRepository.deleteById(key);
    }

    @Async("applicationTaskExecutor")
    public CompletableFuture<SystemSettingResponse> getByKeyAsync(String key) {
        return CompletableFuture.completedFuture(getByKey(key));
    }
}
