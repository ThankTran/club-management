package com.example.demo.role.domain.service.impl;

import com.example.demo.role.domain.service.interfaces.RoleDomainService;
import com.example.demo.role.dto.request.RoleRequest;
import org.springframework.stereotype.Service;

@Service
public class RoleDomainServiceImpl implements RoleDomainService {
    private static final int MIN_ROLE_PRIORITY = 1;
    private static final int MAX_ROLE_PRIORITY = 10;

    @Override
    public void validateCreateRequest(RoleRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Role request must not be empty");
        }
        if (request.getRoleName() == null || request.getRoleName().isBlank()) {
            throw new IllegalArgumentException("Role name must not be empty");
        }
        if (request.getPriority() == null
                || request.getPriority() < MIN_ROLE_PRIORITY
                || request.getPriority() > MAX_ROLE_PRIORITY) {
            throw new IllegalArgumentException("Role priority must be between 1 and 10");
        }
    }

    @Override
    public void validateRoleUniqueness(String roleName, boolean exists) {
        if (exists) {
            throw new IllegalArgumentException("Role already exists: " + roleName);
        }
    }

    @Override
    public void validateDelete(Long roleId, boolean exists, boolean hasMembers) {
        if (!exists) {
            throw new IllegalArgumentException("Role not found: " + roleId);
        }
        if (hasMembers) {
            throw new IllegalArgumentException("Cannot delete role because members still use it.");
        }
    }
}
