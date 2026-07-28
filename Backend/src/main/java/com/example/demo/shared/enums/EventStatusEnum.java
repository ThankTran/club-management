package com.example.demo.shared.enums;

public enum EventStatusEnum {
    NotStarted("Chưa bắt đầu"),
    InProgress("Đang diễn ra"),
    Finished("Đã hoàn thành"),
    Evaluated("đã đánh giá"),
    Cancelled("Đã hủy");

    private final String description;

    EventStatusEnum(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}