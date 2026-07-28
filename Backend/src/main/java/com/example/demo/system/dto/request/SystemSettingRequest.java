package com.example.demo.system.dto.request;

import lombok.Data;

@Data
public class SystemSettingRequest {
    private String settingKey;
    private String settingValue;
    private String description;
    private Long updatedById;
}

