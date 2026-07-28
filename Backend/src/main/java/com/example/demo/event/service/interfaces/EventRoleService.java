package com.example.demo.event.service.interfaces;

import com.example.demo.event.dto.request.EventRoleRequest;
import com.example.demo.event.dto.response.EventRoleResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface EventRoleService {
    EventRoleResponse create(EventRoleRequest request);

    List<EventRoleResponse> getAll();

    EventRoleResponse getByName(String roleName);

    CompletableFuture<List<EventRoleResponse>> getAllAsync();
}
