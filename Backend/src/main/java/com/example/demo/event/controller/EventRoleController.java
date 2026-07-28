package com.example.demo.event.controller;

import com.example.demo.event.dto.request.EventRoleRequest;
import com.example.demo.event.dto.response.EventRoleResponse;
import com.example.demo.event.service.interfaces.EventRoleService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/event-roles")
public class EventRoleController {

    private final EventRoleService eventRoleService;

    public EventRoleController(EventRoleService eventRoleService) {
        this.eventRoleService = eventRoleService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody EventRoleRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(eventRoleService.create(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<EventRoleResponse>> getAll() {
        return ResponseEntity.ok(eventRoleService.getAll());
    }

    @GetMapping("/by-name")
    public ResponseEntity<?> getByName(@RequestParam String name) {
        try {
            return ResponseEntity.ok(eventRoleService.getByName(name));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
