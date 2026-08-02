package com.example.demo.member.dto.request;

import com.example.demo.shared.enums.GraduatedStatusEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JoinClubRequest {
    @NotBlank(message = "Student ID must not be empty")
    @Pattern(regexp = "^[0-9]{8,12}$", message = "Student ID must contain 8-12 digits")
    private String studentId;

    @NotBlank(message = "Full name must not be empty")
    private String fullName;

    @NotNull(message = "Department is required")
    private Long departmentId;

    @NotBlank(message = "Email must not be empty")
    @Email(message = "Email format is invalid")
    private String email;

    @NotBlank(message = "Phone number must not be empty")
    private String phoneNumber;

    @NotBlank(message = "Gender must not be empty")
    private String gender;

    @NotNull(message = "Date of birth must not be empty")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    private GraduatedStatusEnum graduatedStatus;
    private String roleName;
}
