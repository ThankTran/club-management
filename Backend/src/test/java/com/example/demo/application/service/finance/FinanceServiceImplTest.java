package com.example.demo.application.service.finance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo.application.service.finance.impl.FinanceServiceImpl;
import com.example.demo.domain.enums.TransactionType;
import com.example.demo.domain.repository.finance.TransactionRepository;
import com.example.demo.domain.service.finance.FinanceDomainService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class FinanceServiceImplTest {
    @Test
    void getTotalIncomeUsesAggregateRepositoryQuery() {
        TransactionRepository transactionRepository = Mockito.mock(TransactionRepository.class);
        FinanceDomainService financeDomainService = Mockito.mock(FinanceDomainService.class);
        FinanceServiceImpl service = new FinanceServiceImpl(transactionRepository, financeDomainService);
        LocalDateTime from = LocalDateTime.of(2026, 6, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 6, 30, 23, 59, 59);

        when(transactionRepository.sumCompletedAmountByTypeAndDateBetween(TransactionType.INCOME, from, to))
                .thenReturn(BigDecimal.valueOf(123_000L));

        BigDecimal result = service.getTotalIncome(from, to);

        assertEquals(BigDecimal.valueOf(123_000L), result);
        verify(transactionRepository).sumCompletedAmountByTypeAndDateBetween(TransactionType.INCOME, from, to);
    }
}
