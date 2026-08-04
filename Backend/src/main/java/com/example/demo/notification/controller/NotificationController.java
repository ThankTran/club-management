package com.example.demo.notification.controller;

import com.example.demo.notification.dto.request.NotificationRequest;
import com.example.demo.notification.dto.response.NotificationResponse;
import com.example.demo.notification.service.interfaces.NotificationService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationController {

    NotificationService notificationService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody NotificationRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(notificationService.create(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getAll() {
        return ResponseEntity.ok(notificationService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(notificationService.getById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<NotificationResponse>> search(@RequestParam String title) {
        return ResponseEntity.ok(notificationService.searchByTitle(title));
    }

    @GetMapping("/by-target")
    public ResponseEntity<List<NotificationResponse>> byTarget(@RequestParam String targetType) {
        return ResponseEntity.ok(notificationService.getByTargetType(targetType));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        notificationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
