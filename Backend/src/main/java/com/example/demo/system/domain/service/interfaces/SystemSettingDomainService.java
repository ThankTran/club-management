package com.example.demo.system.domain.service.interfaces;

import com.example.demo.system.dto.request.SystemSettingRequest;
import com.example.demo.member.entity.Member;

public interface SystemSettingDomainService {
    void validateCreateOrUpdateRequest(SystemSettingRequest request);

    void validateUpdatedBy(Member updatedBy);

    void validateDelete(String key);
}
