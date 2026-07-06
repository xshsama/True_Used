package com.xsh.trueused.security.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import com.xsh.trueused.security.user.UserPrincipal;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "secret", "test-secret-change-me-change-me-change-me!");
        ReflectionTestUtils.setField(jwtTokenProvider, "expirationMs", 60000L);
        ReflectionTestUtils.setField(jwtTokenProvider, "refreshExpirationMs", 120000L);
        ReflectionTestUtils.setField(jwtTokenProvider, "issuer", "trueused-test");
        ReflectionTestUtils.setField(jwtTokenProvider, "audience", "trueused-web-test");
    }

    @Test
    void shouldIssueJwtRegisteredClaims() {
        UserPrincipal principal = new UserPrincipal(
                1L,
                "alice",
                "alice@example.test",
                "ignored",
                true,
                Set.of(new SimpleGrantedAuthority("ROLE_USER")));

        String token = jwtTokenProvider.generateAccessTokenFromPrincipal(principal);
        var claims = jwtTokenProvider.parseAllClaims(token);

        assertEquals("trueused-test", claims.getIssuer());
        assertEquals("trueused-web-test", claims.getAudience());
        assertNotNull(claims.getId());
    }

    @Test
    void shouldRejectTokenForDifferentAudience() {
        UserPrincipal principal = new UserPrincipal(
                1L,
                "alice",
                "alice@example.test",
                "ignored",
                true,
                Set.of(new SimpleGrantedAuthority("ROLE_USER")));
        String token = jwtTokenProvider.generateAccessTokenFromPrincipal(principal);

        ReflectionTestUtils.setField(jwtTokenProvider, "audience", "other-client");

        assertFalse(jwtTokenProvider.validateToken(token));
    }
}
