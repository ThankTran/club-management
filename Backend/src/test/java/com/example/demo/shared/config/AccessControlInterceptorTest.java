package com.example.demo.shared.config;

import com.example.demo.shared.security.AccessControlInterceptor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.example.demo.user.dto.response.UserResponse;
import com.example.demo.auth.service.interfaces.AuthTokenService;
import com.example.demo.user.service.interfaces.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class AccessControlInterceptorTest {
    private AuthTokenService authTokenService;
    private UserService userService;
    private AccessControlInterceptor interceptor;

    @BeforeEach
    void setUp() {
        authTokenService = Mockito.mock(AuthTokenService.class);
        userService = Mockito.mock(UserService.class);
        interceptor = new AccessControlInterceptor(authTokenService, userService);

        when(authTokenService.parseUserId("Bearer member-token")).thenReturn(99L);
        when(userService.getUserById(99L)).thenReturn(UserResponse.builder()
                .userId(99L)
                .memberId(7L)
                .rolePriority(10)
                .build());
    }

    @Test
    void memberCanCreateDocumentProposal() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request("POST", "/api/documents"), response, new Object());

        assertTrue(allowed);
    }

    @Test
    void memberCanUploadDocumentFileForLaterOwnershipCheck() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request("POST", "/api/document-files"), response, new Object());

        assertTrue(allowed);
    }

    @Test
    void memberCanListAllMembers() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request("GET", "/api/members"), response, new Object());

        assertTrue(allowed);
    }

    @Test
    void memberCanSearchMembers() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request("GET", "/api/members/search"), response, new Object());

        assertTrue(allowed);
    }

    @Test
    void memberCanReadOwnMemberProfile() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request("GET", "/api/members/7"), response, new Object());

        assertTrue(allowed);
    }

    @Test
    void memberCanReadOwnLoginSessions() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request("GET", "/api/users/7/sessions"), response, new Object());

        assertTrue(allowed);
    }

    @Test
    void memberCannotAccessAdminSystemSettings() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request("GET", "/api/system-settings"), response, new Object());

        assertFalse(allowed);
        assertEquals(403, response.getStatus());
    }

    @Test
    void publicCanReadLandingLeadersWithoutToken() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(new MockHttpServletRequest("GET", "/api/members/public-leaders"), response, new Object());

        assertTrue(allowed);
    }

    @Test
    void publicCanReadUpcomingEventsWithoutToken() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(new MockHttpServletRequest("GET", "/api/events/public-upcoming"), response, new Object());

        assertTrue(allowed);
    }

    @Test
    void memberCanAskHelpAi() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request("POST", "/api/ai/help"), response, new Object());

        assertTrue(allowed);
    }

    @Test
    void memberCanRequestDocumentChanges() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(request("POST", "/api/documents/approve"), response, new Object());

        assertTrue(allowed);
    }

    @Test
    void helpAiRequiresAuthentication() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(new MockHttpServletRequest("POST", "/api/ai/help"), response, new Object());

        assertFalse(allowed);
        assertEquals(401, response.getStatus());
    }

    private MockHttpServletRequest request(String method, String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer member-token");
        return request;
    }
}
