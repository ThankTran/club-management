package com.example.demo.department.service.impl;

import com.example.demo.department.dto.request.DepartmentRequest;
import com.example.demo.department.dto.response.DepartmentResponse;
import com.example.demo.department.mapper.DepartmentMapper;
import com.example.demo.department.service.interfaces.DepartmentService;
import com.example.demo.department.repository.DepartmentRepository;
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
@CacheConfig(cacheNames = "departments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DepartmentServiceImpl implements DepartmentService {
    DepartmentRepository departmentRepository;
    MemberRepository memberRepository;
    DepartmentMapper departmentMapper;

    @CacheEvict(allEntries = true)
    public DepartmentResponse create(DepartmentRequest request) {
        if (departmentRepository.existsByDepartmentNameIgnoreCase(request.getDepartmentName())) {
            throw new IllegalArgumentException("Department already exists: " + request.getDepartmentName());
        }
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
        if (!departmentRepository.existsById(id)) {
            throw new IllegalArgumentException("Department not found: " + id);
        }
        if (memberRepository.existsByDepartmentDepartmentId(id)) {
            throw new IllegalArgumentException("Cannot delete department because members still belong to it.");
        }
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
