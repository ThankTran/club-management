package com.example.demo.member.dto.request;

import com.example.demo.shared.enums.GraduatedStatusEnum;
import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JoinClubRequest {
    private String studentId;
    private String fullName;
    private Long departmentId;
    private String email;
    private String phoneNumber;
    private String gender;
    private LocalDate dateOfBirth;
    private GraduatedStatusEnum graduatedStatus;
    private String roleName;
}
