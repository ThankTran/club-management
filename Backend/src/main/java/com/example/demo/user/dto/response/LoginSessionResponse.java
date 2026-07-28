package com.example.demo.user.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginSessionResponse {
    private Long sessionId;
    private Long userId;
    private LocalDateTime loginAt;
    private String ipAddress;
    private String userAgent;
    private String deviceLabel;
}
