package com.example.demo.role.service.impl;

import com.example.demo.role.dto.request.RoleRequest;
import com.example.demo.role.dto.response.RoleResponse;
import com.example.demo.role.mapper.RoleMapper;
import com.example.demo.member.repository.MemberRepository;
import com.example.demo.role.repository.RoleRepository;

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
@CacheConfig(cacheNames = "roles")
public class RoleServiceImpl implements com.example.demo.role.service.interfaces.RoleService {
    private final RoleRepository roleRepository;
    private final MemberRepository memberRepository;
    private final RoleMapper roleMapper;
    public RoleServiceImpl(
            RoleRepository roleRepository,
            MemberRepository memberRepository,
            RoleMapper roleMapper) {
        this.roleRepository = roleRepository;
        this.memberRepository = memberRepository;
        this.roleMapper = roleMapper;
    }

    @CacheEvict(allEntries = true)
    public RoleResponse create(RoleRequest request) {
        if (roleRepository.existsByRoleNameIgnoreCase(request.getRoleName())) {
            throw new IllegalArgumentException("Role already exists: " + request.getRoleName());
        }
        return roleMapper.toResponse(roleRepository.save(roleMapper.toEntity(request)));
    }

    @Cacheable(key = "'all'")
    public List<RoleResponse> getAll() {
        return roleRepository.findAll().stream().map(roleMapper::toResponse).toList();
    }

    @Cacheable(key = "'id:' + #id")
    public RoleResponse getById(Long id) {
        return roleRepository.findById(id).map(roleMapper::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vai trò: " + id));
    }

    @Cacheable(key = "'name:' + #roleName")
    public RoleResponse getByRoleName(String roleName) {
        return roleRepository.findByRoleName(roleName).map(roleMapper::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vai trò: " + roleName));
    }

    @CacheEvict(allEntries = true)
    public void delete(Long id) {
        if (!roleRepository.existsById(id)) {
            throw new IllegalArgumentException("Role not found: " + id);
        }
        if (memberRepository.existsByRoleRoleId(id)) {
            throw new IllegalArgumentException("Cannot delete role because members still use it.");
        }
        roleRepository.deleteById(id);
    }

    @Async("applicationTaskExecutor")
    public CompletableFuture<List<RoleResponse>> getAllAsync() {
        return CompletableFuture.completedFuture(getAll());
    }

    @Async("applicationTaskExecutor")
    public CompletableFuture<RoleResponse> getByIdAsync(Long id) {
        return CompletableFuture.completedFuture(getById(id));
    }
}
