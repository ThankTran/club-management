package com.example.demo.shared.enums;

public enum TransactionStatus {
    PENDING("Chờ duyệt"),
    APPROVED("Đã duyệt"),
    REJECTED("Đã từ chối"),
    CANCELLED("Đã hủy"),
    PROCESSING("Chờ xác nhận"),
    COMPLETED("Đã hoàn thành"),
    FAILED("Đã thất bại"),
    REFUNDED("Đã hoàn trả");

    private final String label;

    TransactionStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
