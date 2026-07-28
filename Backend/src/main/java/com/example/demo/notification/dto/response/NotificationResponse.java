package com.example.demo.notification.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
    private Long notificationId;
    private String title;
    private String content;
    private Long senderId;
    private String targetType;
    private String sendMethod;
    private LocalDateTime sentAt;
}
