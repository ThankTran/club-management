package com.example.demo.member.controller;

import com.example.demo.member.dto.request.ApprovalRequest;
import com.example.demo.member.dto.request.JoinClubRequest;
import com.example.demo.member.dto.request.MemberSearchRequest;
import com.example.demo.department.dto.response.DepartmentResponse;
import com.example.demo.member.dto.response.MemberPublicResponse;
import com.example.demo.member.dto.response.MemberResponse;
import com.example.demo.department.service.interfaces.DepartmentService;
import com.example.demo.member.service.interfaces.MemberService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberUseCase;
    private final DepartmentService departmentService;

    public MemberController(MemberService memberUseCase, DepartmentService departmentService) {
        this.memberUseCase = memberUseCase;
        this.departmentService = departmentService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerMember(@RequestBody JoinClubRequest request) {
        try {
            MemberResponse response = memberUseCase.registerMember(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/approve")
    public ResponseEntity<?> approveMember(@RequestBody ApprovalRequest request) {
        try {
            MemberResponse response = memberUseCase.approveMember(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchMembers(
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String studentId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String reqStatus,
            @RequestParam(required = false) String graduatedStatus,
            @RequestParam(required = false) Integer rolePriority,
            @RequestParam(required = false) Boolean rolePriorityGreaterThan,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            Sort sort = "asc".equalsIgnoreCase(sortDir)
                    ? Sort.by(sortBy).ascending()
                    : Sort.by(sortBy).descending();
            Pageable pageable = PageRequest.of(page, size, sort);

            MemberSearchRequest searchRequest = MemberSearchRequest.builder()
                    .fullName(fullName)
                    .studentId(studentId)
                    .departmentId(departmentId)
                    .reqStatus(reqStatus != null
                            ? com.example.demo.shared.enums.ApprovalStatusEnum.valueOf(reqStatus.toUpperCase())
                            : null)
                    .graduatedStatus(graduatedStatus != null
                            ? com.example.demo.shared.enums.GraduatedStatusEnum.valueOf(graduatedStatus.toUpperCase())
                            : null)
                    .rolePriority(rolePriority)
                    .rolePriorityGreaterThan(rolePriorityGreaterThan)
                    .build();

            Page<MemberResponse> response = memberUseCase.searchMembers(searchRequest, pageable);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllMembers() {
        try {
            List<MemberResponse> response = memberUseCase.getAllMembers();
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/public-leaders")
    public ResponseEntity<List<MemberPublicResponse>> getPublicLeaders() {
        return ResponseEntity.ok(memberUseCase.getPublicLeaders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getMemberById(@PathVariable Long id) {
        try {
            MemberResponse response = memberUseCase.getMemberById(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateMember(@PathVariable Long id,
                                          @RequestBody JoinClubRequest request) {
        try {
            MemberResponse response = memberUseCase.updateMember(id, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMember(@PathVariable Long id) {
        try {
            memberUseCase.deleteMember(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/departments")
    public ResponseEntity<?> getAllDepartments() {
        try {
            List<DepartmentResponse> response = departmentService.getAll();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }
}
