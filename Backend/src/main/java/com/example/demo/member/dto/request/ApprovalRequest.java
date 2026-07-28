package com.example.demo.member.dto.request;

import com.example.demo.shared.enums.ApprovalStatusEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class ApprovalRequest {
    private Long memberId;
    private Long approvedBy;
    private ApprovalStatusEnum status;
    private String note;
}