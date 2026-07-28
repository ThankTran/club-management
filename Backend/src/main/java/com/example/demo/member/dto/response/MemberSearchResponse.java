package com.example.demo.member.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberSearchResponse {

    private Long memberId;
    private String fullName;        
    private String studentId;       
    private LocalDate dateOfBirth; 
    private String departmentName;  
    private String phone;           
    private String email;         
    private String reqStatus;       
    private String graduatedStatus;
}