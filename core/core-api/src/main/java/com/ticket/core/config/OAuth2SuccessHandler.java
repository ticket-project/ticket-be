package com.ticket.core.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final String frontendUrl;

    public OAuth2SuccessHandler(
            final JwtTokenProvider jwtTokenProvider,
            @Value("${oauth2.redirect-url:http://localhost:3000/oauth2/callback}") final String frontendUrl
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void onAuthenticationSuccess(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final Authentication authentication
    ) throws IOException {
        final Object principal = authentication.getPrincipal();
        
        // CustomOAuth2User에서 Member 정보 추출
        if (principal instanceof CustomOAuth2User) {
            final CustomOAuth2User customOAuth2User = (CustomOAuth2User) principal;
            final Long memberId = customOAuth2User.getMemberId();
            final String email = customOAuth2User.getMember().getEmail().getEmail();
            final String role = customOAuth2User.getMember().getRole().name();

            // JWT 토큰 생성
            final String token = jwtTokenProvider.generateToken(memberId, email, role);

            // 프론트엔드로 리다이렉트 (토큰을 쿼리 파라미터로 전달)
            final String redirectUrl = String.format("%s?token=%s&memberId=%d", frontendUrl, token, memberId);
            getRedirectStrategy().sendRedirect(request, response, redirectUrl);
        } else {
            // 예외 처리
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "OAuth2 인증 처리 중 오류가 발생했습니다.");
        }
    }
}
