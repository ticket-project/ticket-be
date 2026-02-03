package com.ticket.core.api.controller;

import com.ticket.core.api.controller.request.TokenRefreshRequest;
import com.ticket.core.api.controller.response.TokenResponse;
import com.ticket.core.enums.Role;
import com.ticket.core.security.JwtTokenProvider;
import com.ticket.core.support.exception.AuthException;
import com.ticket.core.support.exception.ErrorType;
import com.ticket.core.support.response.ApiResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "Auth API")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(final JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Operation(summary = "Refresh access token")
    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refresh(@Valid @RequestBody final TokenRefreshRequest request) {
        try {
            final Claims claims = jwtTokenProvider.parseClaims(request.getRefreshToken());
            if (!jwtTokenProvider.isRefreshToken(claims)) {
                throw new AuthException(ErrorType.AUTHENTICATION_ERROR, "Invalid refresh token type");
            }
            final Long memberId = Long.valueOf(claims.getSubject());
            final Role role = Role.valueOf(claims.get("role", String.class));
            final String accessToken = jwtTokenProvider.createAccessToken(memberId, role);
            final String refreshToken = jwtTokenProvider.createRefreshToken(memberId, role);
            return ApiResponse.success(
                    new TokenResponse(accessToken, refreshToken, "Bearer", jwtTokenProvider.getAccessTokenExpiresIn())
            );
        } catch (JwtException | IllegalArgumentException e) {
            throw new AuthException(ErrorType.AUTHENTICATION_ERROR, "Invalid or expired refresh token");
        }
    }
}
