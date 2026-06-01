package com.example.demo.application.service.system.impl;

import com.example.demo.application.dto.request.system.SystemSettingRequest;
import com.example.demo.application.dto.response.system.SystemSettingResponse;
import com.example.demo.application.mapper.system.SystemSettingMapper;
import com.example.demo.domain.model.member.Member;
import com.example.demo.domain.repository.member.MemberRepository;
import com.example.demo.domain.repository.system.SystemSettingRepository;
import com.example.demo.domain.service.system.SystemSettingDomainService;
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
public class SystemSettingServiceImpl implements com.example.demo.application.service.system.interfaces.SystemSettingService {
    private final SystemSettingRepository systemSettingRepository;
    private final MemberRepository memberRepository;
    private final SystemSettingMapper systemSettingMapper;
    private final SystemSettingDomainService systemSettingDomainService;

    public SystemSettingServiceImpl(
            SystemSettingRepository systemSettingRepository,
            MemberRepository memberRepository,
            SystemSettingMapper systemSettingMapper,
            SystemSettingDomainService systemSettingDomainService) {
        this.systemSettingRepository = systemSettingRepository;
        this.memberRepository = memberRepository;
        this.systemSettingMapper = systemSettingMapper;
        this.systemSettingDomainService = systemSettingDomainService;
    }

    @CacheEvict(allEntries = true)
    public SystemSettingResponse createOrUpdate(SystemSettingRequest request) {
        systemSettingDomainService.validateCreateOrUpdateRequest(request);
        Member updatedBy = null;
        if (request.getUpdatedById() != null) {
            updatedBy = memberRepository.findById(request.getUpdatedById())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thành viên cập nhật: " + request.getUpdatedById()));
            systemSettingDomainService.validateUpdatedBy(updatedBy);
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
        systemSettingDomainService.validateDelete(key);
        systemSettingRepository.deleteById(key);
    }

    @Async("applicationTaskExecutor")
    public CompletableFuture<SystemSettingResponse> getByKeyAsync(String key) {
        return CompletableFuture.completedFuture(getByKey(key));
    }
}
