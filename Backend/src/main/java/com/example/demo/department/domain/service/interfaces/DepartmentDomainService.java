package com.example.demo.department.domain.service.interfaces;

import com.example.demo.department.dto.request.DepartmentRequest;

public interface DepartmentDomainService {
    void validateCreateRequest(DepartmentRequest request);

    void validateDepartmentUniqueness(String departmentName, boolean exists);

    void validateDelete(Long departmentId, boolean exists, boolean hasMembers);
}
