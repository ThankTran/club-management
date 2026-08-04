package com.example.demo.finance.service.impl;

import com.example.demo.shared.enums.TransactionType;
import com.example.demo.finance.repository.TransactionRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;

@Service
@Transactional(readOnly = true)
@CacheConfig(cacheNames = "finance")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FinanceServiceImpl implements com.example.demo.finance.service.interfaces.FinanceService {
    static final TransactionType INCOME_TYPE = TransactionType.INCOME;
    static final TransactionType EXPENSE_TYPE = TransactionType.Expense;
    final TransactionRepository transactionRepository;

    private void validateTimeRange(LocalDateTime from, LocalDateTime to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Time range must not be empty");
        }
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("End time must be on or after start time");
        }
    }

    private void validateEventId(String eventId) {
        if (eventId == null || eventId.isBlank()) {
            throw new IllegalArgumentException("Event ID must not be empty");
        }
    }

    @Cacheable(key = "'income:' + #from + '|' + #to")
    public BigDecimal getTotalIncome(LocalDateTime from, LocalDateTime to) {
        validateTimeRange(from, to);
        return transactionRepository.sumCompletedAmountByTypeAndDateBetween(INCOME_TYPE, from, to);
    }

    @Cacheable(key = "'expense:' + #from + '|' + #to")
    public BigDecimal getTotalExpense(LocalDateTime from, LocalDateTime to) {
        validateTimeRange(from, to);
        return transactionRepository.sumCompletedAmountByTypeAndDateBetween(EXPENSE_TYPE, from, to);
    }

    @Cacheable(key = "'income:event:' + #eventId")
    public BigDecimal getTotalIncomeByEvent(String eventId) {
        validateEventId(eventId);
        return transactionRepository.sumCompletedAmountByTypeAndEventId(INCOME_TYPE, eventId);
    }

    @Cacheable(key = "'expense:event:' + #eventId")
    public BigDecimal getTotalExpenseByEvent(String eventId) {
        validateEventId(eventId);
        return transactionRepository.sumCompletedAmountByTypeAndEventId(EXPENSE_TYPE, eventId);
    }

    @Cacheable(key = "'revenue:' + #from + '|' + #to")
    public BigDecimal getRevenue(LocalDateTime from, LocalDateTime to) {
        return getTotalIncome(from, to).subtract(getTotalExpense(from, to));
    }

    @Cacheable(key = "'revenue:event:' + #eventId")
    public BigDecimal getRevenueByEvent(String eventId) {
        return getTotalIncomeByEvent(eventId).subtract(getTotalExpenseByEvent(eventId));
    }

    @Async("applicationTaskExecutor")
    public CompletableFuture<BigDecimal> getRevenueAsync(LocalDateTime from, LocalDateTime to) {
        return CompletableFuture.completedFuture(getRevenue(from, to));
    }

}
