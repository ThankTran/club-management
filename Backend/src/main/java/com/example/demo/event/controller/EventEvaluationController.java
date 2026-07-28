package com.example.demo.event.controller;

import com.example.demo.event.dto.request.EventEvaluationRequest;
import com.example.demo.event.service.interfaces.EventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/event-evaluations")
public class EventEvaluationController {
    private final EventService eventService;

    public EventEvaluationController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    public ResponseEntity<?> createOrUpdate(@RequestBody EventEvaluationRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(eventService.createOrUpdateEvaluation(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/by-event/{eventId}")
    public ResponseEntity<?> getByEvent(@PathVariable String eventId) {
        try {
            return ResponseEntity.ok(eventService.getEvaluationByEvent(eventId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
