package com.example.demo.member.service.interfaces;

import com.example.demo.member.dto.request.ApprovalRequest;
import com.example.demo.member.dto.request.JoinClubRequest;
import com.example.demo.member.dto.request.MemberSearchRequest;
import com.example.demo.member.dto.response.MemberPublicResponse;
import com.example.demo.member.dto.response.MemberResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemberService {
    MemberResponse registerMember(JoinClubRequest request);

    MemberResponse approveMember(ApprovalRequest request);

    List<MemberResponse> getAllMembers();

    List<MemberPublicResponse> getPublicLeaders();

    Page<MemberResponse> searchMembers(MemberSearchRequest request, Pageable pageable);

    MemberResponse getMemberById(Long memberId);

    MemberResponse updateMember(Long memberId, JoinClubRequest request);

    void deleteMember(Long memberId);

    CompletableFuture<Page<MemberResponse>> searchMembersAsync(MemberSearchRequest request, Pageable pageable);

    CompletableFuture<List<MemberResponse>> getAllMembersAsync();

    CompletableFuture<MemberResponse> getMemberByIdAsync(Long memberId);
}
