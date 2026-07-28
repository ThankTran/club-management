package com.example.demo.member.domain.service.impl;

import com.example.demo.member.domain.service.interfaces.MemberDomainService;
import com.example.demo.shared.enums.ApprovalStatusEnum;
import com.example.demo.shared.enums.GenderEnum;
import com.example.demo.shared.enums.GraduatedStatusEnum;
import com.example.demo.member.entity.Member;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public class MemberDomainServiceImpl implements MemberDomainService {
    @Override
    public void validateNotGraduated(GraduatedStatusEnum status) {
        if (status == null) {
            throw new IllegalArgumentException("Graduated status must not be empty");
        }
        if (status == GraduatedStatusEnum.GRADUATED) {
            throw new IllegalArgumentException("Only non-graduated students can register");
        }
    }

    @Override
    public void validateStudentIdFormat(String studentId) {
        if (studentId == null || studentId.isBlank()) {
            throw new IllegalArgumentException("Student ID must not be empty");
        }
        if (!studentId.matches("^[0-9]{8,12}$")) {
            throw new IllegalArgumentException("Student ID must contain 8-12 digits");
        }
    }

    @Override
    public void validateEmailFormat(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email must not be empty");
        }
        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            throw new IllegalArgumentException("Email format is invalid");
        }
    }

    @Override
    public void validateFullNameFormat(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Full name must not be empty");
        }
        String normalized = fullName.trim().replaceAll("\\s+", " ");
        if (normalized.length() < 2 || normalized.length() > 80) {
            throw new IllegalArgumentException("Full name must contain 2-80 characters");
        }
        if (!normalized.matches("^[\\p{L}][\\p{L}\\s'-]*$")) {
            throw new IllegalArgumentException("Full name must contain letters, spaces, apostrophes, or hyphens only");
        }
    }

    @Override
    public void validatePhoneFormat(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Phone number must not be empty");
        }
        String normalized = phone.replaceAll("[\\s.-]", "");
        if (!normalized.matches("^(0\\d{9}|\\+84\\d{9})$")) {
            throw new IllegalArgumentException("Phone number must be 10 digits starting with 0 or +84 followed by 9 digits");
        }
    }

    @Override
    public void validateDefaultStatus(ApprovalStatusEnum status) {
        if (status != ApprovalStatusEnum.PENDING) {
            throw new IllegalArgumentException("New member request status must be PENDING");
        }
    }

    @Override
    public void validateGender(GenderEnum gender) {
        if (gender == null) {
            throw new IllegalArgumentException("Gender must not be empty");
        }
        if (gender == GenderEnum.OTHER) {
            throw new IllegalArgumentException("Gender must be MALE or FEMALE");
        }
    }

    @Override
    public void validateApproverPermission(Member approver) {
        if (approver.getRole() == null) {
            throw new IllegalArgumentException("Approver role is missing");
        }
        if (approver.getRole().getPriority() == null || approver.getRole().getPriority() > 2) {
            throw new IllegalArgumentException("Approver does not have permission");
        }
    }

    @Override
    public void validateApprovalNotFinalized(Member member) {
        if (member.getReqStatus() != ApprovalStatusEnum.PENDING) {
            throw new IllegalArgumentException("This member request was already finalized");
        }
    }

    @Override
    public void validateApprovalStatus(ApprovalStatusEnum status) {
        if (status == null) {
            throw new IllegalArgumentException("Approval status must not be empty");
        }
        if (status == ApprovalStatusEnum.PENDING) {
            throw new IllegalArgumentException("Approval status must be APPROVED or REJECTED");
        }
    }

    @Override
    public void validateApprovalDate(Member member) {
        if (member.getCreatedAt() != null && LocalDateTime.now().isBefore(member.getCreatedAt())) {
            throw new IllegalArgumentException("Approval date must be on or after register date");
        }
    }

    @Override
    public void validateCanUpdate(Member member) {
        if (member.getReqStatus() == ApprovalStatusEnum.APPROVED) {
            throw new IllegalArgumentException("Approved member information cannot be updated here");
        }
    }

    @Override
    public void applyApproval(Member member, ApprovalStatusEnum status, Member approver, String note) {
        member.setReqStatus(status);
        member.setApprovalNote(note);
        member.setApprover(approver);
        member.setApprovalDate(LocalDateTime.now());
        member.setUpdatedAt(LocalDateTime.now());
    }
}
