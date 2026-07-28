package com.example.demo.shared.enums;

public enum GraduatedStatusEnum {
    ACTIVE("Đang hoạt động"),
    GRADUATED("Đã tốt nghiệp"),
    INACTIVE("Đã rời khỏi câu lạc bộ");
    private final String description;

    GraduatedStatusEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}