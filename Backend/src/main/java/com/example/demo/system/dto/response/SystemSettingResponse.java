package com.example.demo.system.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SystemSettingResponse {
    private String settingKey;
    private String settingValue;
    private String description;
    private Long updatedById;
    private LocalDateTime updatedAt;
}

