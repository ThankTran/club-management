package com.example.demo.application.service.finance.interfaces;

import com.example.demo.application.dto.request.finance.TransactionRequest;
import com.example.demo.application.dto.response.finance.MemberDueResponse;
import com.example.demo.application.dto.response.finance.TransactionResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface TransactionService {
    TransactionResponse create(TransactionRequest request);

    TransactionResponse update(String id, TransactionRequest request);

    List<TransactionResponse> getAll();

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
