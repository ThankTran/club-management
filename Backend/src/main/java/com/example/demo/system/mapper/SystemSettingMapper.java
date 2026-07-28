package com.example.demo.system.mapper;

import com.example.demo.system.dto.request.SystemSettingRequest;
import com.example.demo.system.dto.response.SystemSettingResponse;
import com.example.demo.member.entity.Member;
import com.example.demo.system.entity.SystemSetting;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class SystemSettingMapper {
    public SystemSetting toEntity(SystemSettingRequest request, Member updatedBy) {
        return SystemSetting.builder()
                .settingKey(request.getSettingKey())
                .settingValue(request.getSettingValue())
                .description(request.getDescription())
                .updatedBy(updatedBy)
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public SystemSettingResponse toResponse(SystemSetting entity) {
        return SystemSettingResponse.builder()
                .settingKey(entity.getSettingKey())
                .settingValue(entity.getSettingValue())
                .description(entity.getDescription())
                .updatedById(entity.getUpdatedBy() != null ? entity.getUpdatedBy().getMemberId() : null)
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}

