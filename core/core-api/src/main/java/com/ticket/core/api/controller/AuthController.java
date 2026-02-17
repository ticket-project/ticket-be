package com.ticket.core.api.controller;

import com.ticket.core.config.security.OAuth2EndpointConstants;
import com.ticket.core.support.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Map;

@Tag(name = "Auth", description = "Social auth API")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final String GOOGLE_REGISTRATION_ID = "google";
    private static final String KAKAO_REGISTRATION_ID = "kakao";

    @Operation(summary = "Social login URLs", description = "Returns OAuth2 entry URLs for Google and Kakao")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "URLs returned")
    })
    @GetMapping("/social/urls")
    public ApiResponse<Map<String, String>> getSocialLoginUrls() {
        final String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        return ApiResponse.success(Map.of(
                GOOGLE_REGISTRATION_ID, buildSocialLoginUrl(baseUrl, GOOGLE_REGISTRATION_ID),
                KAKAO_REGISTRATION_ID, buildSocialLoginUrl(baseUrl, KAKAO_REGISTRATION_ID)
        ));
    }

    @Operation(summary = "Logout", description = "Invalidates server session and client should discard JWT token")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Logout handled")
    })
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
