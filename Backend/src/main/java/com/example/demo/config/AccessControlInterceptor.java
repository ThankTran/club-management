package com.example.demo.config;

import com.example.demo.application.dto.response.user.UserResponse;
import com.example.demo.application.service.auth.interfaces.AuthTokenService;
import com.example.demo.application.service.user.interfaces.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AccessControlInterceptor implements HandlerInterceptor {
    public static final String CURRENT_USER_ATTRIBUTE = "currentUser";
    public static final String CURRENT_MEMBER_ID_ATTRIBUTE = "currentMemberId";
    public static final String CURRENT_USER_IS_MANAGER_ATTRIBUTE = "currentUserIsManager";

    private static final int MANAGER_PRIORITY_MAX = 1;

    private final AuthTokenService authTokenService;
    private final UserService userService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public AccessControlInterceptor(AuthTokenService authTokenService, UserService userService) {
        this.authTokenService = authTokenService;
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        String method = request.getMethod();
        String path = request.getRequestURI();

        if ("OPTIONS".equalsIgnoreCase(method) || isPublicEndpoint(method, path)) {
            return true;
        }

        UserResponse currentUser;
        try {
            Long userId = authTokenService.parseUserId(request.getHeader(HttpHeaders.AUTHORIZATION));
            currentUser = userService.getUserById(userId);
        } catch (RuntimeException ex) {
            writeError(response, HttpStatus.UNAUTHORIZED, "Missing or invalid authorization token");
            return false;
        }
        if (currentUser == null) {
            writeError(response, HttpStatus.UNAUTHORIZED, "Missing or invalid authorization token");
            return false;
        }

        boolean manager = isManager(currentUser);
        request.setAttribute(CURRENT_USER_ATTRIBUTE, currentUser);
        request.setAttribute(CURRENT_MEMBER_ID_ATTRIBUTE, currentUser.getMemberId());
        request.setAttribute(CURRENT_USER_IS_MANAGER_ATTRIBUTE, manager);

        if (manager || isMemberAllowed(method, path, currentUser.getMemberId())) {
            return true;
        }

        writeError(response, HttpStatus.FORBIDDEN, "Bạn không có quyền truy cập chức năng này");
        return false;
    }

    private boolean isPublicEndpoint(String method, String path) {
        return ("POST".equalsIgnoreCase(method) && matches(path, "/api/auth/login"))
                || ("POST".equalsIgnoreCase(method) && matches(path, "/api/auth/register"))
                || ("POST".equalsIgnoreCase(method) && matches(path, "/api/auth/logout"))
                || ("POST".equalsIgnoreCase(method) && matches(path, "/api/members/register"))
                || ("GET".equalsIgnoreCase(method) && matches(path, "/api/members/departments"))
                || ("GET".equalsIgnoreCase(method) && matches(path, "/api/members/public-leaders"))
                || ("GET".equalsIgnoreCase(method) && matches(path, "/api/events/public-upcoming"));
    }

    private boolean isMemberAllowed(String method, String path, Long memberId) {
        if ("GET".equalsIgnoreCase(method) && matches(path, "/api/auth/me")) {
            return true;
        }

        if ("GET".equalsIgnoreCase(method) && (
                matches(path, "/api/documents/**")
                        || matches(path, "/api/document-files/by-document/**")
                        || matches(path, "/api/document-types/**")
                        || matches(path, "/api/subjects/**")
                        || matches(path, "/api/events")
                        || matches(path, "/api/events/*")
                        || matches(path, "/api/events/search")
                        || matches(path, "/api/events/by-date-range")
                        || matches(path, "/api/events/*/google-calendar-link")
                        || matches(path, "/api/notifications")
                        || matches(path, "/api/notifications/*")
                        || matches(path, "/api/notifications/search")
                        || matches(path, "/api/notifications/by-target")
                        || matches(path, "/api/members")
                        || matches(path, "/api/members/departments"))) {
            return true;
        }

        if ("GET".equalsIgnoreCase(method) && matchesOwnMember(path, memberId,
                "^/api/members/(\\d+)$",
                "^/api/events/registrations/by-member/(\\d+)$",
                "^/api/event-organizers/by-member/(\\d+)$",
                "^/api/notification-recipients/by-member/(\\d+)$",
                "^/api/transactions/member-dues/(\\d+)$")) {
            return true;
        }

        if ("PATCH".equalsIgnoreCase(method) && matchesOwnMember(path, memberId,
                "^/api/notification-recipients/\\d+/members/(\\d+)/read$")) {
            return true;
        }

        if ("PATCH".equalsIgnoreCase(method) && (
                matches(path, "/api/transactions/*/complete")
                        || matches(path, "/api/transactions/*/submit-payment"))) {
            return true;
        }

        if ("DELETE".equalsIgnoreCase(method) && matchesOwnMember(path, memberId,
                "^/api/notification-recipients/\\d+/members/(\\d+)$",
                "^/api/events/[^/]+/registrations/(\\d+)$")) {
            return true;
        }

        if ("POST".equalsIgnoreCase(method) && (
                matches(path, "/api/events/*/registrations")
                        || matches(path, "/api/ai/help")
                        || matches(path, "/api/documents")
                        || matches(path, "/api/document-files"))) {
            return true;
        }

        return false;
    }

    private boolean isManager(UserResponse user) {
        return user != null && user.getRolePriority() != null && user.getRolePriority() <= MANAGER_PRIORITY_MAX;
    }

    private boolean matches(String path, String pattern) {
        return pathMatcher.match(pattern, path);
    }

    private boolean matchesOwnMember(String path, Long currentMemberId, String... regexes) {
        if (currentMemberId == null) {
            return false;
        }

        for (String regex : regexes) {
            Matcher matcher = Pattern.compile(regex).matcher(path);
            if (matcher.matches()) {
                try {
                    return Objects.equals(currentMemberId, Long.valueOf(matcher.group(1)));
                } catch (NumberFormatException ex) {
                    return false;
                }
            }
        }
        return false;
    }

    private void writeError(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }
}
