package com.example.demo.finance.controller;

import com.example.demo.finance.dto.request.TransactionRequest;
import com.example.demo.finance.dto.response.TransactionResponse;
import com.example.demo.finance.service.interfaces.TransactionService;
import com.example.demo.shared.security.AccessControlInterceptor;
import java.util.List;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody TransactionRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.create(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<Page<TransactionResponse>> getAll(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "transactionDate", "createdAt"));
        return ResponseEntity.ok(transactionService.getPage(type, from, to, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        try {
            return ResponseEntity.ok(transactionService.getById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/by-type")
    public ResponseEntity<?> byType(@RequestParam String type) {
        try {
            return ResponseEntity.ok(transactionService.getByType(type));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody TransactionRequest request) {
        try {
            return ResponseEntity.ok(transactionService.update(id, request));
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().startsWith("Không tìm thấy giao dịch")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/by-event/{eventId}")
    public ResponseEntity<List<TransactionResponse>> byEvent(@PathVariable String eventId) {
        return ResponseEntity.ok(transactionService.getByEvent(eventId));
    }

    @GetMapping("/member-dues/{memberId}")
    public ResponseEntity<?> memberDues(@PathVariable Long memberId) {
        try {
            return ResponseEntity.ok(transactionService.getByMemberDues(memberId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/monthly-dues/pending")
    public ResponseEntity<?> pendingMonthlyDues() {
        try {
            return ResponseEntity.ok(transactionService.getPendingMonthlyDues());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<?> complete(
            @PathVariable String id,
            @RequestAttribute(value = AccessControlInterceptor.CURRENT_MEMBER_ID_ATTRIBUTE, required = false) Long currentMemberId,
            @RequestAttribute(value = AccessControlInterceptor.CURRENT_USER_IS_MANAGER_ATTRIBUTE, required = false) Boolean currentUserIsManager) {
        try {
            return ResponseEntity.ok(transactionService.complete(id, currentMemberId, Boolean.TRUE.equals(currentUserIsManager)));
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().startsWith("Không tìm thấy giao dịch")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/submit-payment")
    public ResponseEntity<?> submitPayment(
            @PathVariable String id,
            @RequestAttribute(value = AccessControlInterceptor.CURRENT_MEMBER_ID_ATTRIBUTE, required = false) Long currentMemberId) {
        try {
            return ResponseEntity.ok(transactionService.submitPayment(id, currentMemberId));
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().startsWith("Không tìm thấy giao dịch")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/reject-payment")
    public ResponseEntity<?> rejectPayment(
            @PathVariable String id,
            @RequestAttribute(value = AccessControlInterceptor.CURRENT_MEMBER_ID_ATTRIBUTE, required = false) Long currentMemberId,
            @RequestAttribute(value = AccessControlInterceptor.CURRENT_USER_IS_MANAGER_ATTRIBUTE, required = false) Boolean currentUserIsManager) {
        try {
            return ResponseEntity.ok(transactionService.rejectPayment(id, currentMemberId, Boolean.TRUE.equals(currentUserIsManager)));
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().startsWith("Không tìm thấy giao dịch")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        try {
            transactionService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
