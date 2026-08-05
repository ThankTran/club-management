package com.example.demo.member.mapper;

import com.example.demo.member.dto.request.CreateMemberRequest;
import com.example.demo.member.dto.request.JoinClubRequest;
import com.example.demo.member.dto.response.MemberResponse;
import com.example.demo.member.dto.response.MemberSearchResponse;
import com.example.demo.department.dto.response.DepartmentResponse;

import com.example.demo.department.entity.Department;
import com.example.demo.member.entity.Member;

import com.example.demo.shared.config.GlobalMapperConfig;
import com.example.demo.shared.enums.GenderEnum;
import com.example.demo.shared.enums.GraduatedStatusEnum;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfig.class, imports = {GenderEnum.class, GraduatedStatusEnum.class})
public abstract class MemberMapper {

    @Mapping(target = "reqStatus", constant = "PENDING")
    public abstract Member toEntity(CreateMemberRequest request);

    @Mapping(target = "phone", source = "phoneNumber")
    @Mapping(target = "gender", expression = "java(request.getGender() != null ? GenderEnum.valueOf(request.getGender().toUpperCase()) : null)")
    @Mapping(target = "graduatedStatus", expression = "java(request.getGraduatedStatus() == null ? GraduatedStatusEnum.ACTIVE : request.getGraduatedStatus())")
    @Mapping(target = "reqStatus", constant = "PENDING")
    public abstract Member toEntity(JoinClubRequest request);

    @Mapping(source = "department.departmentId", target = "departmentId")
    @Mapping(source = "department.departmentName", target = "departmentName")
    @Mapping(source = "role.roleName", target = "roleName")
    @Mapping(source = "approver.fullName", target = "approverName")
    public abstract MemberResponse toResponse(Member member);

    @Mapping(source = "department.departmentName", target = "departmentName")
    public abstract MemberSearchResponse toSearchResponse(Member member);

    public abstract DepartmentResponse toDepartmentResponse(Department department);
}

