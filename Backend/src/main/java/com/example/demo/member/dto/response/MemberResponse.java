package com.example.demo.member.dto.response;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberResponse {
    private Long memberId;
    private String studentId;
    private String fullName;
    private Long departmentId;
    private String departmentName;
    private String email;
    private String phone;
    private String gender;
    private LocalDate dateOfBirth;
    private String roleName;
    private String reqStatus;
    private String graduatedStatus;
    private String approverName;
    private LocalDateTime approvalDate;
    private String approvalNote;
    private LocalDateTime createdAt;
}
