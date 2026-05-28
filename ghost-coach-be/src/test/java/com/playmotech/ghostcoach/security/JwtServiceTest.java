package com.playmotech.ghostcoach.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.IncorrectClaimException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MissingClaimException;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String VALID_SECRET = "this-is-a-test-secret-of-at-least-32-bytes-long-12345";
    private static final String ISSUER = "ghost-coach";
    private static final String AUDIENCE = "ghost-coach-client";

    private JwtService service() {
        return new JwtService(VALID_SECRET, 1, ISSUER, AUDIENCE);
    }

    @Test
    @DisplayName("construct with secret < 32 bytes → IllegalStateException")
    void shortSecretRejected() {
        assertThatThrownBy(() -> new JwtService("short", 24, ISSUER, AUDIENCE))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least 32 bytes");
    }

    @Test
    @DisplayName("construct with null secret → NullPointerException")
    void nullSecretRejected() {
        assertThatThrownBy(() -> new JwtService(null, 24, ISSUER, AUDIENCE))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("construct with zero or negative expiration → IllegalStateException")
    void invalidExpirationRejected() {
        assertThatThrownBy(() -> new JwtService(VALID_SECRET, 0, ISSUER, AUDIENCE))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be positive");
        assertThatThrownBy(() -> new JwtService(VALID_SECRET, -1, ISSUER, AUDIENCE))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("construct with blank issuer or audience → IllegalStateException")
    void blankIssuerOrAudienceRejected() {
        assertThatThrownBy(() -> new JwtService(VALID_SECRET, 1, "", AUDIENCE))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("issuer");
        assertThatThrownBy(() -> new JwtService(VALID_SECRET, 1, ISSUER, " "))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("audience");
    }

    @Test
    @DisplayName("generate + parse round-trip preserves claims incl iss, aud, jti")
    void roundTrip() {
        JwtService svc = service();

        String token = svc.generateToken(42L, "user@example.com");
        Claims claims = svc.parseToken(token);

        assertThat(claims.getSubject()).isEqualTo("user@example.com");
        assertThat(claims.get("userId", Long.class)).isEqualTo(42L);
        assertThat(claims.getIssuer()).isEqualTo(ISSUER);
        assertThat(claims.getAudience()).contains(AUDIENCE);
        assertThat(claims.getId()).isNotBlank();
        assertThat(svc.extractJti(claims)).isEqualTo(claims.getId());
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
    }

    @Test
    @DisplayName("parse expired token → ExpiredJwtException")
    void expiredToken() {
        JwtService svc = service();
        SecretKey key = Keys.hmacShaKeyFor(VALID_SECRET.getBytes(StandardCharsets.UTF_8));

        Instant past = Instant.now().minusSeconds(3600);
        String expiredToken = Jwts.builder()
                .issuer(ISSUER)
                .audience().add(AUDIENCE).and()
                .subject("user@example.com")
                .issuedAt(Date.from(past.minusSeconds(60)))
                .expiration(Date.from(past))
                .signWith(key)
                .compact();

        assertThatThrownBy(() -> svc.parseToken(expiredToken))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    @DisplayName("parse token signed with different key → JwtException")
    void wrongSignature() {
        JwtService svc = service();
        SecretKey otherKey = Keys.hmacShaKeyFor(
                "different-secret-of-at-least-32-bytes-long-1234".getBytes(StandardCharsets.UTF_8));

        String token = Jwts.builder()
                .issuer(ISSUER)
                .audience().add(AUDIENCE).and()
                .subject("attacker@example.com")
                .claim("userId", 1L)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600_000))
                .signWith(otherKey)
                .compact();

        assertThatThrownBy(() -> svc.parseToken(token))
                .isInstanceOf(JwtException.class);
    }

    @Test
    @DisplayName("parse token with wrong issuer → IncorrectClaimException")
    void wrongIssuer() {
        JwtService svc = service();
        SecretKey key = Keys.hmacShaKeyFor(VALID_SECRET.getBytes(StandardCharsets.UTF_8));

        String token = Jwts.builder()
                .issuer("other-service")
                .audience().add(AUDIENCE).and()
                .subject("user@example.com")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600_000))
                .signWith(key)
                .compact();

        assertThatThrownBy(() -> svc.parseToken(token))
                .isInstanceOf(IncorrectClaimException.class);
    }

    @Test
    @DisplayName("parse token with wrong audience → IncorrectClaimException")
    void wrongAudience() {
        JwtService svc = service();
        SecretKey key = Keys.hmacShaKeyFor(VALID_SECRET.getBytes(StandardCharsets.UTF_8));

        String token = Jwts.builder()
                .issuer(ISSUER)
                .audience().add("some-other-client").and()
                .subject("user@example.com")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600_000))
                .signWith(key)
                .compact();

        assertThatThrownBy(() -> svc.parseToken(token))
                .isInstanceOf(IncorrectClaimException.class);
    }

    @Test
    @DisplayName("parse token missing iss/aud (legacy token) → MissingClaimException")
    void missingIssAud() {
        JwtService svc = service();
        SecretKey key = Keys.hmacShaKeyFor(VALID_SECRET.getBytes(StandardCharsets.UTF_8));

        String legacyToken = Jwts.builder()
                .subject("user@example.com")
                .claim("userId", 1L)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600_000))
                .signWith(key)
                .compact();

        assertThatThrownBy(() -> svc.parseToken(legacyToken))
                .isInstanceOf(MissingClaimException.class);
    }

    @Test
    @DisplayName("parse garbage string → JwtException")
    void garbageToken() {
        JwtService svc = service();
        assertThatThrownBy(() -> svc.parseToken("not.a.jwt"))
                .isInstanceOf(JwtException.class);
    }
}
