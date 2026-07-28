package com.example.demo.member.domain.service.interfaces;

import com.example.demo.shared.enums.ApprovalStatusEnum;
import com.example.demo.shared.enums.GenderEnum;
import com.example.demo.shared.enums.GraduatedStatusEnum;
import com.example.demo.member.entity.Member;

public interface MemberDomainService {
    void validateNotGraduated(GraduatedStatusEnum status);

    void validateStudentIdFormat(String studentId);

    void validateEmailFormat(String email);

    void validateFullNameFormat(String fullName);

    void validatePhoneFormat(String phone);

    void validateDefaultStatus(ApprovalStatusEnum status);

    void validateGender(GenderEnum gender);

    void validateApproverPermission(Member approver);

    void validateApprovalNotFinalized(Member member);

    void validateApprovalStatus(ApprovalStatusEnum status);

    void validateApprovalDate(Member member);

    void validateCanUpdate(Member member);

    void applyApproval(Member member, ApprovalStatusEnum status, Member approver, String note);
}
