package com.example.demo.document.dto.request;

import com.example.demo.shared.enums.ApprovalStatusEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DocumentApprovalRequest {
    @NotNull(message = "Document id is required")
    private Long documentId;

    @NotNull(message = "Approver is required")
    private Long approvedBy;

    @NotNull(message = "Approval status is required")
    private ApprovalStatusEnum status;

    private String note;

    private String lookupFolderId;
}
