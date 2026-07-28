package com.example.demo.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberPublicResponse {
    private Long memberId;
    private String fullName;
    private String roleName;
}
