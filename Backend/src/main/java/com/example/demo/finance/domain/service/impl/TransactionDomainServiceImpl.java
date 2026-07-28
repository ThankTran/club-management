package com.example.demo.finance.domain.service.impl;

import com.example.demo.finance.domain.service.interfaces.TransactionDomainService;
import com.example.demo.finance.dto.request.TransactionRequest;
import com.example.demo.shared.enums.TransactionType;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;

@Service
public class TransactionDomainServiceImpl implements TransactionDomainService {
    @Override
    public void validateCreateRequest(TransactionRequest request, TransactionType type) {
        if (request == null) {
            throw new IllegalArgumentException("Transaction request must not be empty");
        }
        if (request.getTransactionId() == null || request.getTransactionId().isBlank()) {
            throw new IllegalArgumentException("Transaction ID must not be empty");
        }
        if (type == null) {
            throw new IllegalArgumentException("Transaction type must not be empty");
        }
        if (request.getMemberId() == null
                && (request.getCounterpartyName() == null || request.getCounterpartyName().isBlank())) {
            throw new IllegalArgumentException("Transaction member or counterparty name is required");
        }
        if (request.getDescription() == null || request.getDescription().isBlank()) {
            throw new IllegalArgumentException("Transaction description must not be empty");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transaction amount must be greater than zero");
        }
    }
}
