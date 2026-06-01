package com.example.demo.controller.finance;

import com.example.demo.application.dto.request.finance.TransactionRequest;
import com.example.demo.application.dto.response.finance.TransactionResponse;
import com.example.demo.application.service.finance.interfaces.TransactionService;
import com.example.demo.config.AccessControlInterceptor;
import java.util.List;
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
    public ResponseEntity<List<TransactionResponse>> getAll() {
        return ResponseEntity.ok(transactionService.getAll());
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
