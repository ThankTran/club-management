package com.example.demo.finance.service.interfaces;

import com.example.demo.finance.dto.request.TransactionRequest;
import com.example.demo.finance.dto.response.MemberDueResponse;
import com.example.demo.finance.dto.response.TransactionResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TransactionService {
    TransactionResponse create(TransactionRequest request);

    TransactionResponse update(String id, TransactionRequest request);

    List<TransactionResponse> getAll();

    Page<TransactionResponse> getPage(String type, LocalDateTime from, LocalDateTime to, Pageable pageable);

    List<TransactionResponse> getByType(String type);

    List<TransactionResponse> getByEvent(String eventId);

    List<TransactionResponse> getByMemberDues(Long memberId);

    List<MemberDueResponse> getPendingMonthlyDues();

    TransactionResponse getById(String id);

    TransactionResponse complete(String id, Long currentMemberId, boolean currentUserIsManager);

    TransactionResponse submitPayment(String id, Long currentMemberId);

    TransactionResponse rejectPayment(String id, Long currentMemberId, boolean currentUserIsManager);

    void delete(String id);

    CompletableFuture<List<TransactionResponse>> getAllAsync();
}
