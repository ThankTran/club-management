package com.example.demo.department.mapper;

import com.example.demo.department.dto.request.DepartmentRequest;
import com.example.demo.department.dto.response.DepartmentResponse;
import com.example.demo.department.entity.Department;
import org.springframework.stereotype.Component;

@Component
public class DepartmentMapper {

    public Department toEntity(DepartmentRequest request) {
        return Department.builder()
                .departmentName(request.getDepartmentName())
                .build();
    }

    public DepartmentResponse toResponse(Department entity) {
        return DepartmentResponse.builder()
                .departmentId(entity.getDepartmentId())
                .departmentName(entity.getDepartmentName())
                .build();
    }
}
