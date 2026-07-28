package com.example.demo.role.service.interfaces;

import com.example.demo.role.dto.request.RoleRequest;
import com.example.demo.role.dto.response.RoleResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface RoleService {
    RoleResponse create(RoleRequest request);

    List<RoleResponse> getAll();

    RoleResponse getById(Long id);

    RoleResponse getByRoleName(String roleName);

    void delete(Long id);

    CompletableFuture<List<RoleResponse>> getAllAsync();

    CompletableFuture<RoleResponse> getByIdAsync(Long id);
}
