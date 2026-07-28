package com.example.demo.finance.service.interfaces;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

public interface FinanceService {
    BigDecimal getTotalIncome(LocalDateTime from, LocalDateTime to);

    BigDecimal getTotalExpense(LocalDateTime from, LocalDateTime to);

    BigDecimal getTotalIncomeByEvent(String eventId);

    BigDecimal getTotalExpenseByEvent(String eventId);

    BigDecimal getRevenue(LocalDateTime from, LocalDateTime to);

    BigDecimal getRevenueByEvent(String eventId);

    CompletableFuture<BigDecimal> getRevenueAsync(LocalDateTime from, LocalDateTime to);
}
