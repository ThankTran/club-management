package com.example.demo.system.repository.interfaces;

import com.example.demo.system.entity.SystemSetting;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemSettingRepository extends JpaRepository<SystemSetting, String> {
    @Override
    @EntityGraph(attributePaths = {"updatedBy"})
    List<SystemSetting> findAll();

    @Override
    @EntityGraph(attributePaths = {"updatedBy"})
    Optional<SystemSetting> findById(String settingKey);

    @EntityGraph(attributePaths = {"updatedBy"})
    List<SystemSetting> findBySettingKeyContainingIgnoreCaseOrderBySettingKeyAsc(String settingKey);

    boolean existsByUpdatedByMemberId(Long memberId);
}
