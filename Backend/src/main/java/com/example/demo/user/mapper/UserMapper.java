package com.example.demo.user.mapper;

import com.example.demo.shared.config.GlobalMapperConfig;
import com.example.demo.user.dto.response.UserPasswordResponse;
import com.example.demo.user.dto.response.UserResponse;
import com.example.demo.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfig.class)
public interface UserMapper {

    @Mapping(source = "member.memberId", target = "memberId")
    @Mapping(source = "member.studentId", target = "studentId")
    @Mapping(source = "member.fullName", target = "fullName")
    @Mapping(source = "member.email", target = "email")
    @Mapping(source = "member.department.departmentId", target = "departmentId")
    @Mapping(source = "member.department.departmentName", target = "departmentName")
    @Mapping(source = "member.role.roleId", target = "roleId")
    @Mapping(source = "member.role.roleName", target = "roleName")
    @Mapping(source = "member.role.priority", target = "rolePriority")
    @Mapping(source = "member.reqStatus", target = "reqStatus")
    @Mapping(source = "member.graduatedStatus", target = "graduatedStatus")
    UserResponse toResponse(User entity);

    @Mapping(source = "member.memberId", target = "memberId")
    UserPasswordResponse toPasswordResponse(User entity);
}

