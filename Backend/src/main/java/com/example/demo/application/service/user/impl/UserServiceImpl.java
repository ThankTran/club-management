package com.example.demo.application.service.user.impl;

import com.example.demo.application.dto.request.user.ChangePasswordRequest;
import com.example.demo.application.dto.request.user.CreateUserRequest;
import com.example.demo.application.dto.request.user.LoginRequest;
import com.example.demo.application.dto.response.user.UserPasswordResponse;
import com.example.demo.application.dto.response.user.UserResponse;
import com.example.demo.application.mapper.user.UserMapper;
import com.example.demo.application.service.notification.interfaces.NotificationDispatchService;
import com.example.demo.domain.model.member.Member;
import com.example.demo.domain.model.user.User;
import com.example.demo.domain.repository.member.MemberRepository;
import com.example.demo.domain.repository.user.UserRepository;
import com.example.demo.domain.service.user.PasswordHasher;
import com.example.demo.domain.service.user.UserDomainService;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@CacheConfig(cacheNames = "users")
public class UserServiceImpl implements com.example.demo.application.service.user.interfaces.UserService {
    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final UserMapper userMapper;
    private final UserDomainService userDomainService;
    private final PasswordHasher passwordHasher;
    private final NotificationDispatchService notificationDispatchService;

    public UserServiceImpl(UserRepository userRepository,
                           MemberRepository memberRepository,
                           UserMapper userMapper,
                           UserDomainService userDomainService,
                           PasswordHasher passwordHasher,
                           NotificationDispatchService notificationDispatchService) {
        this.userRepository = userRepository;
        this.memberRepository = memberRepository;
        this.userMapper = userMapper;
        this.userDomainService = userDomainService;
        this.passwordHasher = passwordHasher;
        this.notificationDispatchService = notificationDispatchService;
    }

    @Override
    @CacheEvict(allEntries = true)
    public UserResponse createUser(CreateUserRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Thông tin tạo tài khoản không được để trống");
        }
        userDomainService.validateCreateRequest(request.getMemberId(), request.getPassword());

        if (userRepository.existsByMemberMemberId(request.getMemberId())) {
            throw new IllegalArgumentException("Thành viên đã có tài khoản");
        }

        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thành viên với ID: " + request.getMemberId()));

        User user = User.create(member, passwordHasher.hash(request.getPassword()));

        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public UserResponse login(LoginRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Thông tin đăng nhập không được để trống");
        }
        userDomainService.validateLoginRequest(
                request.getUserId(),
                request.getMemberId(),
                request.getUsername(),
                request.getPassword());

        User user = resolveUser(request.getUserId(), request.getMemberId(), request.getUsername());
        userDomainService.verifyLogin(user, request.getPassword());
        return userMapper.toResponse(user);
    }

    @Override
    @Cacheable(key = "'id:' + #userId")
    public UserResponse getUserById(Long userId) {
        return userMapper.toResponse(findUserById(userId));
    }

    @Override
    @Cacheable(key = "'member:' + #memberId")
    public UserResponse getUserByMemberId(Long memberId) {
        return userMapper.toResponse(findUserByMemberId(memberId));
    }

    @Override
    @Cacheable(key = "'all'")
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    @CacheEvict(allEntries = true)
    public UserResponse changePassword(Long userId, ChangePasswordRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Thông tin đổi mật khẩu không được để trống");
        }
        User user = findUserById(userId);
        userDomainService.validateChangePasswordRequest(
                request.getCurrentPassword(),
                request.getNewPassword());
        userDomainService.verifyLogin(user, request.getCurrentPassword());
        userDomainService.changePassword(user, request.getNewPassword());
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @CacheEvict(allEntries = true)
    public UserResponse updatePasswordForAdmin(Long userId, String newPassword) {
        validateNewPassword(newPassword);

        User user = findUserById(userId);
        userDomainService.changePassword(user, newPassword);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Cacheable(key = "'password:' + #userId")
    public UserPasswordResponse getPasswordHashForAdmin(Long userId) {
        return userMapper.toPasswordResponse(findUserById(userId));
    }

    @Override
    @CacheEvict(allEntries = true)
    public void deleteUser(Long userId, Long currentUserId) {
        if (userId == null) {
            throw new IllegalArgumentException("Không xác định được userId cần xóa.");
        }
        if (currentUserId != null && userId.equals(currentUserId)) {
            throw new IllegalArgumentException("Không thể xóa tài khoản đang đăng nhập.");
        }

        User user = findUserById(userId);
        try {
            userRepository.delete(user);
            userRepository.flush();

            String username = user.getMember() != null ? user.getMember().getStudentId() : String.valueOf(userId);
            String fullName = user.getMember() != null ? user.getMember().getFullName() : username;

            notificationDispatchService.toManagers(
                    "Tài khoản đã bị xóa",
                    "Đã xóa tài khoản: " + fullName + " (" + username + ").",
                    "USER",
                    null);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException("Không thể xóa tài khoản vì dữ liệu đang được sử dụng ở phiên đăng nhập hoặc hồ sơ liên quan.");
        }
    }

    @Override
    @Async("applicationTaskExecutor")
    public CompletableFuture<UserResponse> getUserByIdAsync(Long userId) {
        return CompletableFuture.completedFuture(getUserById(userId));
    }

    @Override
    @Async("applicationTaskExecutor")
    public CompletableFuture<UserResponse> getUserByMemberIdAsync(Long memberId) {
        return CompletableFuture.completedFuture(getUserByMemberId(memberId));
    }

    private User resolveUser(Long userId, Long memberId, String username) {
        if (username != null && !username.isBlank()) {
            return findUserByUsername(username.trim());
        }
        if (userId != null) {
            return findUserById(userId);
        }
        return findUserByMemberId(memberId);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + userId));
    }

    private User findUserByMemberId(Long memberId) {
        return userRepository.findByMemberMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản của thành viên ID: " + memberId));
    }

    private User findUserByStudentId(String studentId) {
        return userRepository.findByMemberStudentId(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản với tên đăng nhập: " + studentId));
    }

    private User findUserByUsername(String username) {
        if (username.matches("\\d+")) {
            try {
                var userByMemberId = userRepository.findByMemberMemberId(Long.valueOf(username));
                if (userByMemberId.isPresent()) {
                    return userByMemberId.get();
                }
            } catch (NumberFormatException ignored) {
                // Fall back to student ID lookup below.
            }
        }

        return findUserByStudentId(username);
    }

    private void validateNewPassword(String newPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("Mật khẩu mới không được để trống");
        }
        if (newPassword.length() < 6) {
            throw new IllegalArgumentException("Mật khẩu mới phải có ít nhất 6 ký tự");
        }
    }
}
