package com.example.demo.finance.mapper;

import com.example.demo.finance.dto.request.TransactionRequest;
import com.example.demo.finance.dto.response.TransactionResponse;
import com.example.demo.shared.enums.TransactionStatus;
import com.example.demo.shared.enums.TransactionType;
import com.example.demo.event.entity.Event;
import com.example.demo.finance.entity.Transaction;
import com.example.demo.member.entity.Member;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public Transaction toEntity(
            TransactionRequest request,
            Event event,
            Member member,
            Member createdBy,
            Member approvedBy) {
        return Transaction.builder()
                .transactionId(request.getTransactionId())
                .event(event)
                .member(member)
                .counterpartyName(request.getCounterpartyName())
                .type(parseTransactionType(request.getType()))
                .amount(request.getAmount())
                .description(request.getDescription())
                .transactionDate(request.getTransactionDate() == null ? LocalDateTime.now() : request.getTransactionDate())
                .status(request.getStatus() != null ? request.getStatus() : TransactionStatus.PENDING)
                .createdBy(createdBy)
                .approvedBy(approvedBy)
                .build();
    }

    public TransactionResponse toResponse(Transaction entity) {
        return TransactionResponse.builder()
                .transactionId(entity.getTransactionId())
                .eventId(entity.getEvent() == null ? null : entity.getEvent().getEventId())
                .memberId(entity.getMember() == null ? null : entity.getMember().getMemberId())
                .memberName(entity.getMember() == null ? entity.getCounterpartyName() : entity.getMember().getFullName())
                .counterpartyName(entity.getCounterpartyName())
                .type(entity.getType())
                .amount(entity.getAmount())
                .description(entity.getDescription())
                .transactionDate(entity.getTransactionDate())
                .status(entity.getStatus())
                .createdById(entity.getCreatedBy() == null ? null : entity.getCreatedBy().getMemberId())
                .approvedById(entity.getApprovedBy() == null ? null : entity.getApprovedBy().getMemberId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .approvedAt(entity.getApprovedAt())
                .build();
    }

    public TransactionType parseTransactionType(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Loại giao dịch không được để trống");
        }
        String t = raw.trim();
        if ("INCOME".equalsIgnoreCase(t)) {
            return TransactionType.INCOME;
        }
        if ("EXPENSE".equalsIgnoreCase(t)) {
            return TransactionType.Expense;
        }
        throw new IllegalArgumentException("Loại giao dịch chỉ nhận INCOME hoặc EXPENSE");
    }
}
