package com.example.demo.finance.dto.request;

import com.example.demo.shared.enums.TransactionStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class TransactionRequest {
    @NotBlank(message = "Transaction ID must not be empty")
    private String transactionId;

    private String eventId;
    private Long memberId;
    private String counterpartyName;

    @NotBlank(message = "Transaction type must not be empty")
    private String type;

    @NotNull(message = "Transaction amount must not be empty")
    @Positive(message = "Transaction amount must be greater than zero")
    private BigDecimal amount;

    @NotBlank(message = "Transaction description must not be empty")
    private String description;

    private LocalDateTime transactionDate;
    private TransactionStatus status;
    private Long createdById;
    private Long approvedById;
}
