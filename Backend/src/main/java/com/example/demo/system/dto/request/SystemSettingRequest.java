package com.example.demo.system.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SystemSettingRequest {
    @NotBlank(message = "Setting key must not be empty")
    private String settingKey;

    @NotBlank(message = "Setting value must not be empty")
    private String settingValue;

    private String description;
    private Long updatedById;
}

