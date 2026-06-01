package com.example.demo.application.service.role.impl;

import com.example.demo.application.dto.request.role.RoleRequest;
import com.example.demo.application.dto.response.role.RoleResponse;
import com.example.demo.application.mapper.role.RoleMapper;
import com.example.demo.domain.repository.member.MemberRepository;
import com.example.demo.domain.repository.role.RoleRepository;
import com.example.demo.domain.service.role.RoleDomainService;
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
public class RoleServiceImpl implements com.example.demo.application.service.role.interfaces.RoleService {
    private final RoleRepository roleRepository;
    private final MemberRepository memberRepository;
    private final RoleMapper roleMapper;
    private final RoleDomainService roleDomainService;

    public RoleServiceImpl(
            RoleRepository roleRepository,
            MemberRepository memberRepository,
            RoleMapper roleMapper,
            RoleDomainService roleDomainService) {
        this.roleRepository = roleRepository;
        this.memberRepository = memberRepository;
        this.roleMapper = roleMapper;
        this.roleDomainService = roleDomainService;
    }

    @CacheEvict(allEntries = true)
    public RoleResponse create(RoleRequest request) {
        roleDomainService.validateCreateRequest(request);
        roleDomainService.validateRoleUniqueness(
                request.getRoleName(),
                roleRepository.existsByRoleNameIgnoreCase(request.getRoleName()));
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
        roleDomainService.validateDelete(
                id,
                roleRepository.existsById(id),
                memberRepository.existsByRoleRoleId(id));
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
