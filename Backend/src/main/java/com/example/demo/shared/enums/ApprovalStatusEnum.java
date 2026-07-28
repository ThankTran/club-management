package com.example.demo.shared.enums;

public enum ApprovalStatusEnum {
    PENDING("Chờ duyệt"),
    APPROVED("Đã duyệt"),
    REJECTED("Bị từ chối"),
    REQUESTED_CHANGES("Yêu cầu chỉnh sửa");

    private final String value;

    ApprovalStatusEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}