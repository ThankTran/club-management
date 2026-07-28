package com.example.demo.system.service.interfaces;

import com.example.demo.system.dto.request.SystemSettingRequest;
import com.example.demo.system.dto.response.SystemSettingResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface SystemSettingService {
    SystemSettingResponse createOrUpdate(SystemSettingRequest request);

    List<SystemSettingResponse> getAll();

    List<SystemSettingResponse> searchByKey(String key);

    SystemSettingResponse getByKey(String key);

    SystemSettingResponse getByKeyOrDefault(String key, String defaultValue, String description);

    void delete(String key);

    CompletableFuture<SystemSettingResponse> getByKeyAsync(String key);
}
