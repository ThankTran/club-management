package com.example.demo.finance.dto.response;

import com.example.demo.shared.enums.TransactionStatus;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberDueResponse {
    private String transactionId;
    private Long memberId;
    private String studentId;
    private String memberName;
    private String roleName;
    private String month;
    private BigDecimal amount;
    private TransactionStatus status;
}
