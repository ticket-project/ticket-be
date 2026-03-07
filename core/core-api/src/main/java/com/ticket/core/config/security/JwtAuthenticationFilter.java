package com.ticket.core.config.security;

import com.ticket.core.domain.member.MemberPrincipal;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String JWT_ERROR_ATTRIBUTE = "jwt.error";
    private final JwtTokenService jwtTokenService;

    @Override
    protected void doFilterInternal(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final FilterChain filterChain
    ) throws ServletException, IOException {
        final String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (StringUtils.hasText(authorization) && authorization.startsWith(BEARER_PREFIX)) {
            final String token = authorization.substring(BEARER_PREFIX.length());
            try {
                final MemberPrincipal memberPrincipal = jwtTokenService.parse(token);
                final UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                memberPrincipal,
                                null,
                                memberPrincipal.getAuthorities()
                        );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (ExpiredJwtException e) {
                log.debug("JWT 만료: {}", e.getMessage());
                request.setAttribute(JWT_ERROR_ATTRIBUTE, "expired");
            } catch (SignatureException | MalformedJwtException e) {
                log.warn("JWT 위조/변조 시도: {} from IP: {}", e.getMessage(), request.getRemoteAddr());
                request.setAttribute(JWT_ERROR_ATTRIBUTE, "invalid");
            } catch (JwtException | IllegalArgumentException e) {
                log.warn("JWT 인증 실패: {}", e.getMessage());
                request.setAttribute(JWT_ERROR_ATTRIBUTE, "unknown");
            }
        }

        filterChain.doFilter(request, response);
    }
}

