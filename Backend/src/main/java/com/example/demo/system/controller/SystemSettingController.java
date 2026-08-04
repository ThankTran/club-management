package com.example.demo.system.controller;

import com.example.demo.system.dto.request.SystemSettingRequest;
import com.example.demo.system.dto.response.SystemSettingResponse;
import com.example.demo.system.service.interfaces.SystemSettingService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;

@RestController
@RequestMapping("/api/system-settings")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SystemSettingController {
    static final String MONTHLY_DUE_SETTING_KEY = "finance.monthlyDueAmount";
    static final String MONTHLY_DUE_DEFAULT_VALUE = "50000";
    static final String MONTHLY_DUE_DESCRIPTION = "Số tiền đóng quỹ định kỳ";

    final SystemSettingService systemSettingService;

    @PostMapping
    public ResponseEntity<?> createOrUpdate(@RequestBody SystemSettingRequest request) {
        try {
            return ResponseEntity.ok(systemSettingService.createOrUpdate(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<SystemSettingResponse>> getAll() {
        return ResponseEntity.ok(systemSettingService.getAll());
    }

    @GetMapping("/search")
    public ResponseEntity<List<SystemSettingResponse>> search(@RequestParam String key) {
        return ResponseEntity.ok(systemSettingService.searchByKey(key));
    }

    @GetMapping("/by-key")
    public ResponseEntity<?> getByKey(@RequestParam String key) {
        try {
            return ResponseEntity.ok(systemSettingService.getByKey(key));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/monthly-due-amount")
    public ResponseEntity<SystemSettingResponse> getMonthlyDueAmount() {
        return ResponseEntity.ok(systemSettingService.getByKeyOrDefault(
                MONTHLY_DUE_SETTING_KEY,
                MONTHLY_DUE_DEFAULT_VALUE,
                MONTHLY_DUE_DESCRIPTION));
    }

    @PostMapping("/monthly-due-amount")
    public ResponseEntity<?> saveMonthlyDueAmount(@RequestBody SystemSettingRequest request) {
        try {
            String value = request == null ? null : request.getSettingValue();
            if (value == null || !value.matches("\\d+")) {
                return ResponseEntity.badRequest().body("Số tiền đóng quỹ không hợp lệ");
            }

            SystemSettingRequest normalized = new SystemSettingRequest();
            normalized.setSettingKey(MONTHLY_DUE_SETTING_KEY);
            normalized.setSettingValue(value);
            normalized.setDescription(MONTHLY_DUE_DESCRIPTION);
            normalized.setUpdatedById(request.getUpdatedById());

            return ResponseEntity.ok(systemSettingService.createOrUpdate(normalized));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@RequestParam String key) {
        systemSettingService.delete(key);
        return ResponseEntity.noContent().build();
    }
}
