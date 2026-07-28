package com.example.demo.shared.enums;

public enum TransactionType {
    INCOME("Thu"),
    Expense("Chi");

    private final String label;

    TransactionType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}