package com.example.demo.department.domain.service.impl;

import com.example.demo.department.domain.service.interfaces.DepartmentDomainService;
import com.example.demo.department.dto.request.DepartmentRequest;
import org.springframework.stereotype.Service;

@Service
public class DepartmentDomainServiceImpl implements DepartmentDomainService {
    @Override
    public void validateCreateRequest(DepartmentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Department request must not be empty");
        }
        if (request.getDepartmentName() == null || request.getDepartmentName().isBlank()) {
            throw new IllegalArgumentException("Department name must not be empty");
        }
    }

    @Override
    public void validateDepartmentUniqueness(String departmentName, boolean exists) {
        if (exists) {
            throw new IllegalArgumentException("Department already exists: " + departmentName);
        }
    }

    @Override
    public void validateDelete(Long departmentId, boolean exists, boolean hasMembers) {
        if (!exists) {
            throw new IllegalArgumentException("Department not found: " + departmentId);
        }
        if (hasMembers) {
            throw new IllegalArgumentException("Cannot delete department because members still belong to it.");
        }
    }
}
