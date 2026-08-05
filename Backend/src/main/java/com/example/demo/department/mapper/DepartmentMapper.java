package com.example.demo.department.mapper;

import com.example.demo.department.dto.request.DepartmentRequest;
import com.example.demo.department.dto.response.DepartmentResponse;
import com.example.demo.department.entity.Department;
import com.example.demo.shared.config.GlobalMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = GlobalMapperConfig.class)
public interface DepartmentMapper {
    Department toEntity(DepartmentRequest request);
    DepartmentResponse toResponse(Department entity);
}

