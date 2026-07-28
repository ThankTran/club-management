package com.example.demo.finance.service.impl;

import com.example.demo.shared.enums.TransactionType;
import com.example.demo.finance.repository.interfaces.TransactionRepository;
import com.example.demo.finance.domain.service.interfaces.FinanceDomainService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@CacheConfig(cacheNames = "finance")
public class FinanceServiceImpl implements com.example.demo.finance.service.interfaces.FinanceService {
    private static final TransactionType INCOME_TYPE = TransactionType.INCOME;
    private static final TransactionType EXPENSE_TYPE = TransactionType.Expense;
    private final TransactionRepository transactionRepository;
    private final FinanceDomainService financeDomainService;

    public FinanceServiceImpl(
            TransactionRepository transactionRepository,
            FinanceDomainService financeDomainService) {
        this.transactionRepository = transactionRepository;
        this.financeDomainService = financeDomainService;
    }

    @Cacheable(key = "'income:' + #from + '|' + #to")
    public BigDecimal getTotalIncome(LocalDateTime from, LocalDateTime to) {
        financeDomainService.validateTimeRange(from, to);
        return transactionRepository.sumCompletedAmountByTypeAndDateBetween(INCOME_TYPE, from, to);
    }

    @Cacheable(key = "'expense:' + #from + '|' + #to")
    public BigDecimal getTotalExpense(LocalDateTime from, LocalDateTime to) {
        financeDomainService.validateTimeRange(from, to);
        return transactionRepository.sumCompletedAmountByTypeAndDateBetween(EXPENSE_TYPE, from, to);
    }

    @Cacheable(key = "'income:event:' + #eventId")
    public BigDecimal getTotalIncomeByEvent(String eventId) {
        financeDomainService.validateEventId(eventId);
        return transactionRepository.sumCompletedAmountByTypeAndEventId(INCOME_TYPE, eventId);
    }

    @Cacheable(key = "'expense:event:' + #eventId")
    public BigDecimal getTotalExpenseByEvent(String eventId) {
        financeDomainService.validateEventId(eventId);
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
