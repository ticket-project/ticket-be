package com.ticket.core.config.security;

import com.ticket.core.domain.member.MemberPrincipal;
import com.ticket.core.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtTokenService {

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtTokenService(final JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        if (!StringUtils.hasText(jwtProperties.getSecretKey())) {
            throw new IllegalArgumentException("security.jwt.secret-key must not be blank");
        }

        final byte[] keyBytes = jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("security.jwt.secret-key must be at least 32 bytes for HS256");
        }
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String createAccessToken(final MemberPrincipal memberPrincipal) {
        final Instant now = Instant.now();
        final Instant expiresAt = now.plusSeconds(jwtProperties.getAccessTokenExpirationSeconds());

        return Jwts.builder()
                .issuer(jwtProperties.getIssuer())
                .subject(String.valueOf(memberPrincipal.getMemberId()))
                .claim("role", memberPrincipal.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(secretKey)
                .compact();
    }

    public MemberPrincipal parse(final String token) {
        final Claims claims = Jwts.parser()
                .requireIssuer(jwtProperties.getIssuer())
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        final Long memberId = Long.parseLong(claims.getSubject());
        final Role role = Role.valueOf(claims.get("role", String.class));
        return new MemberPrincipal(memberId, role);
    }

    public long getAccessTokenExpirationSeconds() {
        return jwtProperties.getAccessTokenExpirationSeconds();
    }
}
