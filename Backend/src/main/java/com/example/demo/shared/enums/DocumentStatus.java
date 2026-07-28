package com.example.demo.shared.enums;

public enum DocumentStatus {
    WORKING("Đang hoạt động"),
    FIXING("Đang sửa"),
    CANCELLED("Đã hủy");

    private final String label;

    DocumentStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}