package com.example.demo.department.service.interfaces;

import com.example.demo.department.dto.request.DepartmentRequest;
import com.example.demo.department.dto.response.DepartmentResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface DepartmentService {
    DepartmentResponse create(DepartmentRequest request);

    List<DepartmentResponse> getAll();

    DepartmentResponse getById(Long id);

    DepartmentResponse getByDepartmentName(String departmentName);

    void delete(Long id);

    CompletableFuture<List<DepartmentResponse>> getAllAsync();

    CompletableFuture<DepartmentResponse> getByIdAsync(Long id);
}
