package com.example.demo.finance.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo.finance.dto.response.TransactionResponse;
import com.example.demo.finance.mapper.TransactionMapper;
import com.example.demo.finance.service.impl.TransactionServiceImpl;
import com.example.demo.notification.service.interfaces.NotificationDispatchService;
import com.example.demo.shared.enums.TransactionStatus;
import com.example.demo.shared.enums.TransactionType;
import com.example.demo.finance.entity.Transaction;
import com.example.demo.member.entity.Member;
import com.example.demo.event.repository.EventRepository;
import com.example.demo.finance.repository.TransactionRepository;
import com.example.demo.member.repository.MemberRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class TransactionServiceImplTest {
    @Test
    void submitPaymentMarksIncomeAsAwaitingConfirmationWithoutProcessingStatus() {
        TransactionRepository transactionRepository = Mockito.mock(TransactionRepository.class);
        EventRepository eventRepository = Mockito.mock(EventRepository.class);
        MemberRepository memberRepository = Mockito.mock(MemberRepository.class);
        TransactionMapper transactionMapper = Mockito.mock(TransactionMapper.class);
        NotificationDispatchService notificationDispatchService = Mockito.mock(NotificationDispatchService.class);

        TransactionServiceImpl service = new TransactionServiceImpl(
                transactionRepository,
                eventRepository,
                memberRepository,
                transactionMapper,
                notificationDispatchService);

        Member member = Member.builder().memberId(5L).fullName("Vo Duc Tai").build();
        Transaction transaction = Transaction.builder()
                .transactionId("DUE-FUND-202606-5")
                .member(member)
                .type(TransactionType.INCOME)
                .status(TransactionStatus.PENDING)
                .build();

        when(transactionRepository.findById("DUE-FUND-202606-5")).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionMapper.toResponse(any(Transaction.class)))
                .thenReturn(TransactionResponse.builder().transactionId("DUE-FUND-202606-5").build());

        service.submitPayment("DUE-FUND-202606-5", 5L);

        assertEquals(TransactionStatus.PENDING, transaction.getStatus());
        assertNull(transaction.getApprovedBy());
        assertNotNull(transaction.getApprovedAt());
        verify(notificationDispatchService)
                .toManagersAndMembers(any(), any(), any(), any(), any());
    }

    @Test
    void getPageReturnsMappedTransactionsFromPagedRepositoryQuery() {
        TransactionRepository transactionRepository = Mockito.mock(TransactionRepository.class);
        EventRepository eventRepository = Mockito.mock(EventRepository.class);
        MemberRepository memberRepository = Mockito.mock(MemberRepository.class);
        TransactionMapper transactionMapper = Mockito.mock(TransactionMapper.class);
        NotificationDispatchService notificationDispatchService = Mockito.mock(NotificationDispatchService.class);

        TransactionServiceImpl service = new TransactionServiceImpl(
                transactionRepository,
                eventRepository,
                memberRepository,
                transactionMapper,
                notificationDispatchService);

        Transaction transaction = Transaction.builder()
                .transactionId("THU001")
                .type(TransactionType.INCOME)
                .status(TransactionStatus.COMPLETED)
                .build();
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Transaction> transactionPage = new PageImpl<>(List.of(transaction), pageable, 1);

        when(transactionRepository.findActivePage(
                TransactionType.INCOME,
                null,
                null,
                pageable)).thenReturn(transactionPage);
        when(transactionMapper.toResponse(transaction))
                .thenReturn(TransactionResponse.builder().transactionId("THU001").build());

        Page<TransactionResponse> result = service.getPage("INCOME", null, null, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("THU001", result.getContent().get(0).getTransactionId());
    }
}
