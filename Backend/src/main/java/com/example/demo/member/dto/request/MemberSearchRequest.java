package com.example.demo.member.dto.request;

import com.example.demo.shared.enums.ApprovalStatusEnum;
import com.example.demo.shared.enums.GraduatedStatusEnum;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MemberSearchRequest {
    private String fullName;        
    private String studentId;       
    private Long departmentId;   
    private ApprovalStatusEnum reqStatus;      
    private GraduatedStatusEnum graduatedStatus; 
    private Integer rolePriority;
    private Boolean rolePriorityGreaterThan;
}
