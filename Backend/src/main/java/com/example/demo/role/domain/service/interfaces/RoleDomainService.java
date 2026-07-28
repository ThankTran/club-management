package com.example.demo.role.domain.service.interfaces;

import com.example.demo.role.dto.request.RoleRequest;

public interface RoleDomainService {
    void validateCreateRequest(RoleRequest request);

    void validateRoleUniqueness(String roleName, boolean exists);

    void validateDelete(Long roleId, boolean exists, boolean hasMembers);
}
