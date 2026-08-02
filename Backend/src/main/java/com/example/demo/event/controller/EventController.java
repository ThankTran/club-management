package com.example.demo.event.controller;

import com.example.demo.event.dto.request.EventRequest;
import com.example.demo.event.dto.response.EventCalendarLinkResponse;
import com.example.demo.event.dto.response.EventPublicResponse;
import com.example.demo.event.dto.response.EventResponse;
import com.example.demo.event.service.interfaces.EventService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody EventRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(eventService.create(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @Valid @RequestBody EventRequest request) {
        try {
            return ResponseEntity.ok(eventService.update(id, request));
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().startsWith("Không tìm thấy sự kiện")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<EventResponse>> getAll() {
        return ResponseEntity.ok(eventService.getAll());
    }

    @GetMapping("/public-upcoming")
    public ResponseEntity<List<EventPublicResponse>> getPublicUpcomingEvents() {
        return ResponseEntity.ok(eventService.getPublicUpcomingEvents());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        try {
            return ResponseEntity.ok(eventService.getById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/{id}/google-calendar-link")
    public ResponseEntity<?> getGoogleCalendarLink(@PathVariable String id) {
        try {
            EventCalendarLinkResponse response = eventService.getGoogleCalendarLink(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().startsWith("Không tìm thấy sự kiện")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<EventResponse>> search(@RequestParam String name) {
        return ResponseEntity.ok(eventService.searchByName(name));
    }

    @GetMapping("/by-date-range")
    public ResponseEntity<List<EventResponse>> byDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(eventService.getByDateRange(from, to));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        try {
            eventService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().startsWith("Không tìm thấy sự kiện")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
