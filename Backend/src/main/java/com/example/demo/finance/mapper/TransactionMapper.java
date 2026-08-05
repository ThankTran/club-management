package com.example.demo.finance.mapper;

import com.example.demo.finance.dto.request.TransactionRequest;
import com.example.demo.finance.dto.response.TransactionResponse;
import com.example.demo.shared.config.GlobalMapperConfig;
import com.example.demo.shared.enums.TransactionType;
import com.example.demo.event.entity.Event;
import com.example.demo.finance.entity.Transaction;
import com.example.demo.member.entity.Member;
import java.time.LocalDateTime;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfig.class, imports = {LocalDateTime.class})
public abstract class TransactionMapper {

    @Mapping(target = "event", source = "event")
    @Mapping(target = "member", source = "member")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "approvedBy", source = "approvedBy")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "transactionId", source = "request.transactionId")
    @Mapping(target = "counterpartyName", source = "request.counterpartyName")
    @Mapping(target = "amount", source = "request.amount")
    @Mapping(target = "description", source = "request.description")
    @Mapping(target = "transactionDate", expression = "java(request.getTransactionDate() == null ? LocalDateTime.now() : request.getTransactionDate())")
    @Mapping(target = "status", source = "request.status")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "approvedAt", ignore = true)
    public abstract Transaction toEntity(
            TransactionRequest request,
            Event event,
            Member member,
            Member createdBy,
            Member approvedBy,
            TransactionType type);

    @Mapping(source = "event.eventId", target = "eventId")
    @Mapping(source = "member.memberId", target = "memberId")
    @Mapping(source = "createdBy.memberId", target = "createdById")
    @Mapping(source = "approvedBy.memberId", target = "approvedById")
    @Mapping(target = "memberName", expression = "java(entity.getMember() == null ? entity.getCounterpartyName() : entity.getMember().getFullName())")
    public abstract TransactionResponse toResponse(Transaction entity);
}
