package com.example.demo.notification.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NotificationRequest {
    @NotBlank(message = "Notification title must not be empty")
    private String title;

    @NotBlank(message = "Notification content must not be empty")
    private String content;

    private Long senderId;

    @NotBlank(message = "Notification target type must not be empty")
    private String targetType;

    @NotBlank(message = "Notification send method must not be empty")
    private String sendMethod;
}
