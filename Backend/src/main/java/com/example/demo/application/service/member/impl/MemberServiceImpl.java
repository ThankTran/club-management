package com.example.demo.application.service.member.impl;

import com.example.demo.application.dto.request.member.JoinClubRequest;
import com.example.demo.application.dto.request.member.ApprovalRequest;
import com.example.demo.application.dto.request.member.MemberSearchRequest;
import com.example.demo.application.dto.response.member.MemberPublicResponse;
import com.example.demo.application.dto.response.member.MemberResponse;
import com.example.demo.application.mapper.member.MemberMapper;
import com.example.demo.application.service.notification.interfaces.NotificationDispatchService;
import com.example.demo.application.service.system.interfaces.SystemSettingService;
import com.example.demo.domain.model.member.Member;
import com.example.demo.domain.model.department.Department;
import com.example.demo.domain.model.role.Role;
import com.example.demo.domain.repository.member.MemberRepository;
import com.example.demo.domain.repository.department.DepartmentRepository;
import com.example.demo.domain.repository.role.RoleRepository;
import com.example.demo.domain.enums.ApprovalStatusEnum;
import com.example.demo.domain.enums.GenderEnum;
import com.example.demo.domain.enums.GraduatedStatusEnum;
import com.example.demo.domain.service.member.MemberDomainService;

import java.time.LocalDate;
import java.time.Period;
import java.text.Normalizer;
import java.util.concurrent.CompletableFuture;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@CacheConfig(cacheNames = "members")
public class MemberServiceImpl implements com.example.demo.application.service.member.interfaces.MemberService {
    private static final int MIN_ROLE_PRIORITY = 1;
    private static final int MAX_ROLE_PRIORITY = 10;
    private static final String DEFAULT_ROLE_NAME = "Thành viên";
    private static final String AGE_MIN_SETTING_KEY = "member.age.min";
    private static final String AGE_MAX_SETTING_KEY = "member.age.max";
    private static final int DEFAULT_MIN_AGE = 18;
    private static final int DEFAULT_MAX_AGE = 30;
    private static final String TARGET_MEMBER = "MEMBER";

    private final MemberRepository memberRepository;
    private final DepartmentRepository departmentRepository;
    private final RoleRepository roleRepository;
    private final MemberMapper memberMapper;
    private final MemberDomainService memberDomainService; 
    private final NotificationDispatchService notificationDispatchService;
    private final SystemSettingService systemSettingService;

    public MemberServiceImpl(MemberRepository memberRepository,
                             DepartmentRepository departmentRepository,
                             RoleRepository roleRepository,
                             MemberMapper memberMapper,
                             MemberDomainService memberDomainService,
                             NotificationDispatchService notificationDispatchService,
                             SystemSettingService systemSettingService) {
        this.memberRepository = memberRepository;
        this.departmentRepository = departmentRepository;
        this.roleRepository = roleRepository;
        this.memberMapper = memberMapper;
        this.memberDomainService = memberDomainService;
        this.notificationDispatchService = notificationDispatchService;
        this.systemSettingService = systemSettingService;
    }


    @Override
    @CacheEvict(allEntries = true)
    public MemberResponse registerMember(JoinClubRequest request) {
        if (request.getGraduatedStatus() == null) {
            request.setGraduatedStatus(GraduatedStatusEnum.ACTIVE);
        }

        // Validate format — domain service lo
        memberDomainService.validateStudentIdFormat(request.getStudentId());
        memberDomainService.validateFullNameFormat(request.getFullName());
        memberDomainService.validateEmailFormat(request.getEmail());
        memberDomainService.validatePhoneFormat(request.getPhoneNumber());
        memberDomainService.validateNotGraduated(request.getGraduatedStatus());
        memberDomainService.validateGender(
                GenderEnum.valueOf(request.getGender().toUpperCase())
        );
        validateDateOfBirth(request.getDateOfBirth());

        if (memberRepository.existsByStudentId(request.getStudentId())) {
            throw new IllegalArgumentException("MSSV đã tồn tại trong hệ thống");
        }
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email đã tồn tại trong hệ thống");
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new IllegalArgumentException("Khoa không tồn tại trong hệ thống"));

        Member member = memberMapper.toEntity(request);
        member.setDepartment(department);
        member.setRole(resolveRoleOrDefault(request.getRoleName()));
        member.setReqStatus(ApprovalStatusEnum.PENDING);
        member.setCreatedAt(LocalDateTime.now());
        member.setUpdatedAt(LocalDateTime.now());


        memberDomainService.validateDefaultStatus(member.getReqStatus());

        Member savedMember = memberRepository.save(member);
        notificationDispatchService.toManagers(
                "Yêu cầu thành viên mới",
                savedMember.getFullName() + " vừa gửi yêu cầu tham gia câu lạc bộ.",
                TARGET_MEMBER,
                savedMember);
        return memberMapper.toResponse(savedMember);
    }

    @Override
    @CacheEvict(allEntries = true)
    public MemberResponse approveMember(ApprovalRequest request) {

        // Validate request cơ bản
        if (request.getMemberId() == null)
            throw new IllegalArgumentException("Member ID không được để trống");
        if (request.getApprovedBy() == null)
            throw new IllegalArgumentException("Approver ID không được để trống");

        // QĐ2.4: Validate status
        memberDomainService.validateApprovalStatus(request.getStatus());

        // Lấy member
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hồ sơ thành viên"));

        // QĐ2.1: Validate quyền approver
        Member approver = memberRepository.findById(request.getApprovedBy())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người xét duyệt"));

        // QĐ2.3: Không duyệt lại
        memberDomainService.validateApprovalNotFinalized(member);

        // QĐ2.5: Validate ngày
        memberDomainService.validateApprovalDate(member);

        // Validate note
        if (request.getNote() != null && request.getNote().length() > 500)
            throw new IllegalArgumentException("Ghi chú không được vượt quá 500 ký tự");

        memberDomainService.applyApproval(member, request.getStatus(), approver, request.getNote());
        Member updatedMember = memberRepository.save(member);
        notificationDispatchService.toMembers(
                List.of(updatedMember),
                request.getStatus() == ApprovalStatusEnum.APPROVED
                        ? "Hồ sơ thành viên đã được duyệt"
                        : "Hồ sơ thành viên đã bị từ chối",
                request.getStatus() == ApprovalStatusEnum.APPROVED
                        ? "Yêu cầu tham gia câu lạc bộ của bạn đã được duyệt."
                        : "Yêu cầu tham gia câu lạc bộ của bạn đã bị từ chối.",
                TARGET_MEMBER,
                approver);

        return memberMapper.toResponse(updatedMember);
    }

    @Override
    @Cacheable(key = "'search:' + (#request?.studentId ?: '') + '|' + (#request?.fullName ?: '') + '|' + (#request?.departmentId ?: '') + '|' + (#request?.reqStatus ?: '') + '|' + (#request?.graduatedStatus ?: '') + '|' + (#request?.rolePriority ?: '') + '|' + (#request?.rolePriorityGreaterThan ?: '') + '|p:' + #pageable.pageNumber + '|s:' + #pageable.pageSize + '|sort:' + #pageable.sort.toString()")
    public Page<MemberResponse> searchMembers(MemberSearchRequest request, Pageable pageable) {
        MemberSearchRequest safeRequest = request == null ? MemberSearchRequest.builder().build() : request;
        return filterMembers(safeRequest, pageable).map(memberMapper::toResponse);
    }

    private Page<Member> filterMembers(MemberSearchRequest request, Pageable pageable) {
        String studentId = normalizeSearchText(request.getStudentId());
        String fullName = normalizeSearchText(request.getFullName());
        validateRolePriorityFilter(request);

        return memberRepository.searchMembers(
                studentId,
                fullName,
                request.getDepartmentId(),
                request.getReqStatus(),
                request.getGraduatedStatus(),
                request.getRolePriority(),
                request.getRolePriorityGreaterThan(),
                pageable);
    }

    private void validateRolePriorityFilter(MemberSearchRequest request) {
        if (request.getRolePriority() == null) {
            return;
        }
        if (request.getRolePriority() < MIN_ROLE_PRIORITY || request.getRolePriority() > MAX_ROLE_PRIORITY) {
            throw new IllegalArgumentException("Role priority must be between 1 and 10");
        }
    }

    private String normalizeSearchText(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }

    private String normalizeRoleName(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .trim()
                .toLowerCase();
    }

    private void validateDateOfBirth(LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            throw new IllegalArgumentException("Ngày sinh không được để trống");
        }

        LocalDate today = LocalDate.now();
        if (!dateOfBirth.isBefore(today)) {
            throw new IllegalArgumentException("Ngày sinh phải nhỏ hơn ngày hiện tại");
        }

        int minAge = getAgeSetting(AGE_MIN_SETTING_KEY, DEFAULT_MIN_AGE);
        int maxAge = getAgeSetting(AGE_MAX_SETTING_KEY, DEFAULT_MAX_AGE);
        int age = Period.between(dateOfBirth, today).getYears();
        if (age < minAge || age > maxAge) {
            throw new IllegalArgumentException(
                    "Tuổi phải nằm trong khoảng từ " + minAge + " đến " + maxAge + " tuổi");
        }
    }

    private int getAgeSetting(String key, int defaultValue) {
        try {
            String value = systemSettingService.getByKeyOrDefault(key, String.valueOf(defaultValue), "").getSettingValue();
            return Integer.parseInt(value);
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    @Override
    @Cacheable(key = "'all'")
    public List<MemberResponse> getAllMembers() {
        return memberRepository.findAll().stream()
                .map(memberMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(key = "'public-leaders'")
    public List<MemberPublicResponse> getPublicLeaders() {
        return memberRepository.findAll().stream()
                .filter(member -> member.getReqStatus() == ApprovalStatusEnum.APPROVED)
                .filter(member -> member.getRole() != null)
                .filter(member -> !"thanh vien".equals(normalizeRoleName(member.getRole().getRoleName())))
                .map(member -> MemberPublicResponse.builder()
                        .memberId(member.getMemberId())
                        .fullName(member.getFullName())
                        .roleName(member.getRole().getRoleName())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(key = "'id:' + #memberId")
    public MemberResponse getMemberById(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy thành viên với ID: " + memberId));
        return memberMapper.toResponse(member);
    }

    @Override
    @CacheEvict(allEntries = true)
    public MemberResponse updateMember(Long memberId, JoinClubRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy thành viên với ID: " + memberId));
        if (request.getGraduatedStatus() == null) {
            request.setGraduatedStatus(GraduatedStatusEnum.ACTIVE);
        }

        // Validate email nếu thay đổi
        memberDomainService.validateFullNameFormat(request.getFullName());
        memberDomainService.validateEmailFormat(request.getEmail());
        memberDomainService.validatePhoneFormat(request.getPhoneNumber());
        memberDomainService.validateNotGraduated(request.getGraduatedStatus());
        validateDateOfBirth(request.getDateOfBirth());
        if (!member.getEmail().equals(request.getEmail())
                && memberRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email đã tồn tại trong hệ thống");
        }

        // Validate department
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new IllegalArgumentException("Khoa không tồn tại"));

        member.setFullName(request.getFullName());
        member.setEmail(request.getEmail());
        member.setPhone(request.getPhoneNumber());
        member.setGender(GenderEnum.valueOf(request.getGender().toUpperCase()));
        member.setDateOfBirth(request.getDateOfBirth());
        member.setDepartment(department);
        member.setGraduatedStatus(request.getGraduatedStatus());
        member.setRole(resolveRoleOrDefault(request.getRoleName()));
        member.setUpdatedAt(LocalDateTime.now());

        Member savedMember = memberRepository.save(member);
        if (savedMember.getReqStatus() == ApprovalStatusEnum.PENDING) {
            notificationDispatchService.toManagers(
                    "Hồ sơ thành viên cần xét duyệt",
                    savedMember.getFullName() + " vừa cập nhật hồ sơ và đang chờ xét duyệt.",
                    TARGET_MEMBER,
                    savedMember);
        }

        return memberMapper.toResponse(savedMember);
    }

    @Override
    @CacheEvict(allEntries = true)
    public void deleteMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy thành viên với ID: " + memberId));
        try {
            memberRepository.delete(member);
            memberRepository.flush();

            notificationDispatchService.toManagers(
                    "Thành viên đã bị xóa",
                    "Đã xóa thành viên: " + member.getFullName() + " (" + member.getStudentId() + ").",
                    TARGET_MEMBER,
                    null);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException(
                    "Không thể xóa thành viên vì dữ liệu đang được sử dụng ở sự kiện, giao dịch hoặc hồ sơ liên quan.");
        }
    }

    private Role resolveRoleOrDefault(String roleName) {
        String safeRoleName = roleName == null || roleName.isBlank() ? DEFAULT_ROLE_NAME : roleName.trim();
        return roleRepository.findByRoleName(safeRoleName)
                .or(() -> findRoleAlias(safeRoleName))
                .orElseThrow(() -> new IllegalArgumentException("Vai trò không tồn tại: " + safeRoleName));
    }

    private java.util.Optional<Role> findRoleAlias(String roleName) {
        if ("Trưởng ban học thuật".equalsIgnoreCase(roleName)) {
            return roleRepository.findByRoleName("Trưởng ban học tập");
        }
        return java.util.Optional.empty();
    }

    @Async("applicationTaskExecutor")
    public CompletableFuture<Page<MemberResponse>> searchMembersAsync(MemberSearchRequest request, Pageable pageable) {
        return CompletableFuture.completedFuture(searchMembers(request, pageable));
    }

    @Async("applicationTaskExecutor")
    public CompletableFuture<List<MemberResponse>> getAllMembersAsync() {
        return CompletableFuture.completedFuture(getAllMembers());
    }

    @Async("applicationTaskExecutor")
    public CompletableFuture<MemberResponse> getMemberByIdAsync(Long memberId) {
        return CompletableFuture.completedFuture(getMemberById(memberId));
    }
}
