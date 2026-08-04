package com.example.demo.event.controller;

import com.example.demo.event.dto.request.EventAttendanceRequest;
import com.example.demo.event.dto.request.EventRegistrationRequest;
import com.example.demo.event.dto.response.EventRegistrationResponse;
import com.example.demo.event.service.interfaces.EventRegistrationService;
import com.example.demo.shared.security.AccessControlInterceptor;
import java.util.List;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EventRegistrationController {
    EventRegistrationService eventRegistrationService;

    @PostMapping("/{eventId}/registrations")
    public ResponseEntity<?> register(
            @PathVariable String eventId,
            @RequestBody EventRegistrationRequest request,
            @RequestAttribute(value = AccessControlInterceptor.CURRENT_MEMBER_ID_ATTRIBUTE, required = false) Long currentMemberId,
            @RequestAttribute(value = AccessControlInterceptor.CURRENT_USER_IS_MANAGER_ATTRIBUTE, required = false) Boolean currentUserIsManager) {
        try {
            Long requestedMemberId = request == null ? null : request.getMemberId();
            if (!Boolean.TRUE.equals(currentUserIsManager) && !Objects.equals(currentMemberId, requestedMemberId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Bạn không có quyền đăng ký cho thành viên khác");
            }
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(eventRegistrationService.register(eventId, requestedMemberId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{eventId}/registrations")
    public ResponseEntity<List<EventRegistrationResponse>> getByEvent(@PathVariable String eventId) {
        return ResponseEntity.ok(eventRegistrationService.getByEvent(eventId));
    }

    @GetMapping("/registrations/by-member/{memberId}")
    public ResponseEntity<List<EventRegistrationResponse>> getByMember(@PathVariable Long memberId) {
        return ResponseEntity.ok(eventRegistrationService.getByMember(memberId));
    }

    @PutMapping("/{eventId}/attendance")
    public ResponseEntity<?> updateAttendance(
            @PathVariable String eventId,
            @RequestBody EventAttendanceRequest request) {
        try {
            return ResponseEntity.ok(eventRegistrationService.updateAttendance(eventId, request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{eventId}/registrations/{memberId}")
    public ResponseEntity<?> unregister(@PathVariable String eventId, @PathVariable Long memberId) {
        try {
            eventRegistrationService.unregister(eventId, memberId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
