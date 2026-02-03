package com.ticket.core.security;

import com.ticket.core.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_TYPE = "type";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    private final JwtProperties properties;
    private final SecretKey key;

    public JwtTokenProvider(final JwtProperties properties) {
        this.properties = properties;
        this.key = resolveKey(properties.getSecret());
    }

    public String createAccessToken(final Long memberId, final Role role) {
        return createToken(memberId, role, properties.getExpiration(), TYPE_ACCESS);
    }

    public String createRefreshToken(final Long memberId, final Role role) {
        return createToken(memberId, role, properties.getRefreshExpiration(), TYPE_REFRESH);
    }

    public Claims parseClaims(final String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isAccessToken(final Claims claims) {
        return TYPE_ACCESS.equals(claims.get(CLAIM_TYPE, String.class));
    }

    public boolean isRefreshToken(final Claims claims) {
        return TYPE_REFRESH.equals(claims.get(CLAIM_TYPE, String.class));
    }

    public long getAccessTokenExpiresIn() {
        return properties.getExpiration();
    }

    private String createToken(final Long memberId, final Role role, final long expirationMs, final String type) {
        final Instant now = Instant.now();
        final Instant expiry = now.plusMillis(expirationMs);
        return Jwts.builder()
                .issuer(properties.getIssuer())
                .subject(String.valueOf(memberId))
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .claim(CLAIM_ROLE, role.name())
                .claim(CLAIM_TYPE, type)
                .signWith(key)
                .compact();
    }

    private SecretKey resolveKey(final String secret) {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secret);
        } catch (IllegalArgumentException e) {
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("JWT secret key must be at least 256 bits");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
