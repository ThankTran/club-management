package com.example.demo.system.mapper;

import com.example.demo.system.dto.request.SystemSettingRequest;
import com.example.demo.system.dto.response.SystemSettingResponse;
import com.example.demo.member.entity.Member;
import com.example.demo.shared.config.GlobalMapperConfig;
import com.example.demo.system.entity.SystemSetting;
import java.time.LocalDateTime;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfig.class, imports = {LocalDateTime.class})
public interface SystemSettingMapper {

    @Mapping(target = "updatedBy", source = "updatedBy")
    @Mapping(target = "updatedAt", expression = "java(LocalDateTime.now())")
    SystemSetting toEntity(SystemSettingRequest request, Member updatedBy);

    @Mapping(source = "updatedBy.memberId", target = "updatedById")
    SystemSettingResponse toResponse(SystemSetting entity);
}


