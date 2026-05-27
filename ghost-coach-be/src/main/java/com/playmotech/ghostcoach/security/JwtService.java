package com.playmotech.ghostcoach.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

@Service
public class JwtService {

    private static final int MIN_SECRET_BYTES = 32; // 256 bits required for HS256

    private final SecretKey key;
    private final Duration expiration;
    private final String issuer;
    private final String audience;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-hours}") int expirationHours,
            @Value("${app.jwt.issuer:ghost-coach}") String issuer,
            @Value("${app.jwt.audience:ghost-coach-client}") String audience) {
        Objects.requireNonNull(secret, "app.jwt.secret must be set");
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "app.jwt.secret must be at least " + MIN_SECRET_BYTES
                            + " bytes (256 bits) for HS256. Current: " + secretBytes.length + " bytes");
        }
        if (expirationHours <= 0) {
            throw new IllegalStateException(
                    "app.jwt.expiration-hours must be positive. Current: " + expirationHours);
        }
        if (issuer == null || issuer.isBlank()) {
            throw new IllegalStateException("app.jwt.issuer must be non-blank");
        }
        if (audience == null || audience.isBlank()) {
            throw new IllegalStateException("app.jwt.audience must be non-blank");
        }

        this.key = Keys.hmacShaKeyFor(secretBytes);
        this.expiration = Duration.ofHours(expirationHours);
        this.issuer = issuer;
        this.audience = audience;
    }

    public String generateToken(Long userId, String email) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(issuer)
                .audience().add(audience).and()
                .subject(email)
                .id(UUID.randomUUID().toString())
                .claim("userId", userId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expiration)))
                .signWith(key)
                .compact();
    }

    public Claims parseToken(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(issuer)
                .requireAudience(audience)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extract the {@code jti} (JWT ID) from parsed claims. Prepared for a future
     * revocation list — not used yet.
     */
    public String extractJti(Claims claims) {
        return claims.getId();
    }
}
