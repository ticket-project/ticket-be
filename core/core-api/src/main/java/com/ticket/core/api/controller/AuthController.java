package com.ticket.core.api.controller;

import com.ticket.core.api.controller.docs.AuthControllerDocs;
import com.ticket.core.api.controller.request.AddMemberRequest;
import com.ticket.core.api.controller.request.LoginMemberRequest;
import com.ticket.core.api.controller.request.OAuth2TokenExchangeRequest;
import com.ticket.core.api.controller.request.RefreshTokenRequest;
import com.ticket.core.api.controller.response.AuthLoginResponse;
import com.ticket.core.config.security.JwtProperties;
import com.ticket.core.config.security.JwtTokenService;
import com.ticket.core.config.security.OAuth2EndpointConstants;
import com.ticket.core.domain.auth.AuthService;
import com.ticket.core.domain.auth.OAuth2AuthCodeService;
import com.ticket.core.domain.auth.RefreshTokenService;
import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.MemberFinder;
import com.ticket.core.domain.member.MemberPrincipal;
import com.ticket.core.enums.Role;
import com.ticket.core.support.exception.AuthException;
import com.ticket.core.support.exception.ErrorType;
import com.ticket.core.support.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController implements AuthControllerDocs {

    private static final String GOOGLE_REGISTRATION_ID = "google";
    private static final String KAKAO_REGISTRATION_ID = "kakao";
    private static final String TOKEN_TYPE_BEARER = "Bearer";

    private final AuthService authService;
    private final JwtTokenService jwtTokenService;
    private final JwtProperties jwtProperties;
    private final RefreshTokenService refreshTokenService;
    private final OAuth2AuthCodeService oAuth2AuthCodeService;
    private final MemberFinder memberFinder;

    @Override
    @PostMapping("/signup")
    public ApiResponse<Long> signUp(@Valid @RequestBody final AddMemberRequest request) {
        final Long memberId = authService.register(request.toAddMember());
        return ApiResponse.success(memberId);
    }

    @Override
    @PostMapping("/login")
    public ApiResponse<AuthLoginResponse> login(@Valid @RequestBody final LoginMemberRequest request) {
        try {
            final Member member = authService.login(request.getEmail(), request.getPassword());
            return ApiResponse.success(issueTokens(member.getId(), member.getRole().name()));
        } catch (AuthException e) {
            throw e;
        }
    }

    @Override
    @PostMapping("/refresh")
    public ApiResponse<AuthLoginResponse> refresh(@Valid @RequestBody final RefreshTokenRequest request) {
        final Long memberId = refreshTokenService.validate(request.getRefreshToken())
                .orElseThrow(() -> new AuthException(ErrorType.AUTHENTICATION_ERROR, "유효하지 않거나 만료된 리프레시 토큰입니다."));

        final Member member = memberFinder.findActiveMemberById(memberId);

        final String newRefreshToken = refreshTokenService.rotate(
                request.getRefreshToken(), memberId, jwtProperties.getRefreshTokenExpirationSeconds());
        final MemberPrincipal principal = new MemberPrincipal(member.getId(), member.getRole());
        final String newAccessToken = jwtTokenService.createAccessToken(principal);
        return ApiResponse.success(new AuthLoginResponse(
                newAccessToken,
                newRefreshToken,
                TOKEN_TYPE_BEARER,
                jwtTokenService.getAccessTokenExpirationSeconds(),
                member.getId()
        ));
    }

    @Override
    @PostMapping("/oauth2/token")
    public ApiResponse<AuthLoginResponse> exchangeOAuth2Token(@Valid @RequestBody final OAuth2TokenExchangeRequest request) {
        final Long memberId = oAuth2AuthCodeService.consumeCode(request.getCode())
                .orElseThrow(() -> new AuthException(ErrorType.AUTHENTICATION_ERROR, "유효하지 않거나 만료된 인증 코드입니다."));

        final Member member = memberFinder.findActiveMemberById(memberId);
        return ApiResponse.success(issueTokens(member.getId(), member.getRole().name()));
    }

    @Override
    @GetMapping("/social/urls")
    public ApiResponse<Map<String, String>> getSocialLoginUrls() {
        final String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        return ApiResponse.success(Map.of(
                GOOGLE_REGISTRATION_ID, buildSocialLoginUrl(baseUrl, GOOGLE_REGISTRATION_ID),
                KAKAO_REGISTRATION_ID, buildSocialLoginUrl(baseUrl, KAKAO_REGISTRATION_ID)
        ));
    }

    @Override
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @AuthenticationPrincipal final MemberPrincipal principal,
            @Valid @RequestBody final RefreshTokenRequest request
    ) {
        final Long tokenOwnerId = refreshTokenService.validateWithoutConsume(request.getRefreshToken())
                .orElseThrow(() -> new AuthException(ErrorType.AUTHENTICATION_ERROR, "유효하지 않은 리프레시 토큰입니다."));
        if (!tokenOwnerId.equals(principal.getMemberId())) {
            throw new AuthException(ErrorType.AUTHORIZATION_ERROR, "본인의 토큰만 무효화할 수 있습니다.");
        }
        // Refresh Token만 무효화 (Access Token은 짧은 만료로 자연 무효화)
        refreshTokenService.revoke(request.getRefreshToken());
        return ApiResponse.success();
    }

    private AuthLoginResponse issueTokens(final Long memberId, final String roleName) {
        final MemberPrincipal principal = new MemberPrincipal(
                memberId, Role.valueOf(roleName));
        final String accessToken = jwtTokenService.createAccessToken(principal);
        final String refreshToken = refreshTokenService.createRefreshToken(
                memberId, jwtProperties.getRefreshTokenExpirationSeconds());

        return new AuthLoginResponse(
                accessToken,
                refreshToken,
                TOKEN_TYPE_BEARER,
                jwtTokenService.getAccessTokenExpirationSeconds(),
                memberId
        );
    }

    private String buildSocialLoginUrl(final String baseUrl, final String registrationId) {
        return baseUrl + OAuth2EndpointConstants.AUTHORIZATION_BASE_URI + "/" + registrationId;
    }
}