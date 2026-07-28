package com.example.demo.notification.dto.request;

import lombok.Data;

@Data
public class NotificationRequest {
    private String title;
    private String content;
    private Long senderId;
    private String targetType;
    private String sendMethod;
}
