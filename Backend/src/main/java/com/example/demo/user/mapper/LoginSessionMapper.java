package com.example.demo.user.mapper;

import com.example.demo.shared.config.GlobalMapperConfig;
import com.example.demo.user.dto.response.LoginSessionResponse;
import com.example.demo.user.entity.LoginSession;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfig.class)
public interface LoginSessionMapper {
    @Mapping(source = "user.userId", target = "userId")
    LoginSessionResponse toResponse(LoginSession session);
}

