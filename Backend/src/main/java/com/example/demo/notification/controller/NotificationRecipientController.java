package com.example.demo.notification.controller;

import com.example.demo.notification.dto.request.NotificationRecipientRequest;
import com.example.demo.notification.dto.response.NotificationRecipientResponse;
import com.example.demo.notification.service.interfaces.NotificationRecipientService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;

@RestController
@RequestMapping("/api/notification-recipients")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationRecipientController {

    NotificationRecipientService notificationRecipientService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody NotificationRecipientRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(notificationRecipientService.create(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/by-notification/{notificationId}")
    public ResponseEntity<List<NotificationRecipientResponse>> byNotification(@PathVariable Long notificationId) {
        return ResponseEntity.ok(notificationRecipientService.getByNotification(notificationId));
    }

    @GetMapping("/by-member/{memberId}")
    public ResponseEntity<List<NotificationRecipientResponse>> byMember(@PathVariable Long memberId) {
        return ResponseEntity.ok(notificationRecipientService.getByMember(memberId));
    }

    @PatchMapping("/{notificationId}/members/{memberId}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long notificationId, @PathVariable Long memberId) {
        try {
            return ResponseEntity.ok(notificationRecipientService.markAsRead(notificationId, memberId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/{notificationId}/members/{memberId}")
    public ResponseEntity<Void> delete(@PathVariable Long notificationId, @PathVariable Long memberId) {
        notificationRecipientService.delete(notificationId, memberId);
        return ResponseEntity.noContent().build();
    }
}
