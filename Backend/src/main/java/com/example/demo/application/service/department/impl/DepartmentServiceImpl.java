package com.example.demo.application.service.department.impl;

import com.example.demo.application.dto.request.department.DepartmentRequest;
import com.example.demo.application.dto.response.department.DepartmentResponse;
import com.example.demo.application.mapper.department.DepartmentMapper;
import com.example.demo.application.service.department.interfaces.DepartmentService;
import com.example.demo.domain.repository.department.DepartmentRepository;
import com.example.demo.domain.repository.member.MemberRepository;
import com.example.demo.domain.service.department.DepartmentDomainService;
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
@CacheConfig(cacheNames = "departments")
public class DepartmentServiceImpl implements DepartmentService {
    private final DepartmentRepository departmentRepository;
    private final MemberRepository memberRepository;
    private final DepartmentMapper departmentMapper;
    private final DepartmentDomainService departmentDomainService;

    public DepartmentServiceImpl(
            DepartmentRepository departmentRepository,
            MemberRepository memberRepository,
            DepartmentMapper departmentMapper,
            DepartmentDomainService departmentDomainService) {
        this.departmentRepository = departmentRepository;
        this.memberRepository = memberRepository;
        this.departmentMapper = departmentMapper;
        this.departmentDomainService = departmentDomainService;
    }

    @CacheEvict(allEntries = true)
    public DepartmentResponse create(DepartmentRequest request) {
        departmentDomainService.validateCreateRequest(request);
        departmentDomainService.validateDepartmentUniqueness(
                request.getDepartmentName(),
                departmentRepository.existsByDepartmentNameIgnoreCase(request.getDepartmentName()));
        return departmentMapper.toResponse(departmentRepository.save(departmentMapper.toEntity(request)));
    }

    @Cacheable(key = "'all'")
    public List<DepartmentResponse> getAll() {
        return departmentRepository.findAll().stream().map(departmentMapper::toResponse).toList();
    }

    @Cacheable(key = "'id:' + #id")
    public DepartmentResponse getById(Long id) {
        return departmentRepository.findById(id).map(departmentMapper::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khoa: " + id));
    }

    @Cacheable(key = "'name:' + #departmentName")
    public DepartmentResponse getByDepartmentName(String departmentName) {
        return departmentRepository.findByDepartmentName(departmentName).map(departmentMapper::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khoa: " + departmentName));
    }

    @CacheEvict(allEntries = true)
    public void delete(Long id) {
        departmentDomainService.validateDelete(
                id,
                departmentRepository.existsById(id),
                memberRepository.existsByDepartmentDepartmentId(id));
        departmentRepository.deleteById(id);
    }

    @Async("applicationTaskExecutor")
    public CompletableFuture<List<DepartmentResponse>> getAllAsync() {
        return CompletableFuture.completedFuture(getAll());
    }

    @Async("applicationTaskExecutor")
    public CompletableFuture<DepartmentResponse> getByIdAsync(Long id) {
        return CompletableFuture.completedFuture(getById(id));
    }
}
