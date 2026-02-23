package com.ticket.core.api.controller;

import com.ticket.core.api.controller.docs.AuthControllerDocs;
import com.ticket.core.api.controller.request.AddMemberRequest;
import com.ticket.core.api.controller.request.LoginMemberRequest;
import com.ticket.core.api.controller.response.AuthLoginResponse;
import com.ticket.core.config.security.JwtTokenService;
import com.ticket.core.config.security.OAuth2EndpointConstants;
import com.ticket.core.domain.auth.AuthService;
import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.MemberPrincipal;
import com.ticket.core.support.response.ApiResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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

    @Override
    @PostMapping("/signup")
    public ApiResponse<Long> signUp(@Valid @RequestBody final AddMemberRequest request) {
        final Long memberId = authService.register(request.toAddMember());
        return ApiResponse.success(memberId);
    }

    @Override
    @PostMapping("/login")
    public ApiResponse<AuthLoginResponse> login(@Valid @RequestBody final LoginMemberRequest request) {
        final Member member = authService.login(request.getEmail(), request.getPassword());
        final MemberPrincipal principal = new MemberPrincipal(member.getId(), member.getRole());
        final String accessToken = jwtTokenService.createAccessToken(principal);

        return ApiResponse.success(new AuthLoginResponse(
                accessToken,
                TOKEN_TYPE_BEARER,
                jwtTokenService.getAccessTokenExpirationSeconds(),
                member.getId()
        ));
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
    public ApiResponse<Void> logout(final HttpServletRequest request, final HttpServletResponse response) {
        final HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        expireSessionCookie(request, response);
        return ApiResponse.success();
    }

    private String buildSocialLoginUrl(final String baseUrl, final String registrationId) {
        return baseUrl + OAuth2EndpointConstants.AUTHORIZATION_BASE_URI + "/" + registrationId;
    }

    private void expireSessionCookie(final HttpServletRequest request, final HttpServletResponse response) {
        final Cookie cookie = new Cookie("JSESSIONID", null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        cookie.setSecure(request.isSecure());
        response.addCookie(cookie);
    }
}
