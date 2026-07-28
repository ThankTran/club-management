package com.example.demo.finance.domain.service.interfaces;

import com.example.demo.finance.dto.request.TransactionRequest;
import com.example.demo.shared.enums.TransactionType;

public interface TransactionDomainService {
    void validateCreateRequest(TransactionRequest request, TransactionType type);
}
