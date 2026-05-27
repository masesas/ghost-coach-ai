package com.playmotech.ghostcoach.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class JwtAuthFilterTest {

    private static final String SECRET = "this-is-a-test-secret-of-at-least-32-bytes-long-12345";
    private static final String ISSUER = "ghost-coach";
    private static final String AUDIENCE = "ghost-coach-client";

    private JwtService jwtService;
    private JwtAuthFilter filter;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, 1, ISSUER, AUDIENCE);
        filter = new JwtAuthFilter(jwtService);
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("no Authorization header → no auth context, chain continues")
    void noAuthHeader() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/v1/users/me");
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(req, res, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(req, res);
    }

    @Test
    @DisplayName("non-Bearer scheme → no auth context")
    void nonBearerScheme() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/v1/users/me");
        req.addHeader("Authorization", "Basic ZGVtbzpkZW1v");
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(req, res, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("valid Bearer token → SecurityContext populated with UserPrincipal")
    void validBearerToken() throws Exception {
        String token = jwtService.generateToken(42L, "user@example.com");
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/v1/users/me");
        req.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(req, res, chain);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isInstanceOf(UserPrincipal.class);
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        assertThat(principal.id()).isEqualTo(42L);
        assertThat(principal.email()).isEqualTo("user@example.com");
        assertThat(auth.getAuthorities()).anyMatch(a -> a.getAuthority().equals("ROLE_USER"));
    }

    @Test
    @DisplayName("malformed token → SecurityContext stays empty, chain continues")
    void malformedToken() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/v1/users/me");
        req.addHeader("Authorization", "Bearer not.a.valid.jwt");
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(req, res, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(req, res);
    }

    @Test
    @DisplayName("token signed with wrong key → SecurityContext empty")
    void wrongSignature() throws Exception {
        JwtService otherService = new JwtService(
                "different-secret-of-at-least-32-bytes-long-1234567", 1, ISSUER, AUDIENCE);
        String token = otherService.generateToken(99L, "other@example.com");

        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/v1/users/me");
        req.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(req, res, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
