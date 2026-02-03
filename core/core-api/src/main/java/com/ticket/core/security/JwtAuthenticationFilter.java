package com.ticket.core.security;

import com.ticket.core.support.exception.ErrorType;
import com.ticket.core.support.response.ApiResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final JsonMapper jsonMapper;

    public JwtAuthenticationFilter(final JwtTokenProvider jwtTokenProvider, final JsonMapper jsonMapper) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.jsonMapper = jsonMapper;
    }

    @Override
    protected void doFilterInternal(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final FilterChain filterChain
    ) throws ServletException, IOException {
        final String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            final String token = header.substring(7);
            try {
                final Claims claims = jwtTokenProvider.parseClaims(token);
                if (!jwtTokenProvider.isAccessToken(claims)) {
                    throw new JwtException("Invalid token type");
                }
                final Long memberId = Long.valueOf(claims.getSubject());
                final String role = claims.get("role", String.class);
                final List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
                final Authentication authentication = new UsernamePasswordAuthenticationToken(memberId, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (JwtException | IllegalArgumentException e) {
                SecurityContextHolder.clearContext();
                writeError(response, ErrorType.AUTHENTICATION_ERROR, "Invalid or expired token");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private void writeError(final HttpServletResponse response, final ErrorType errorType, final Object data) throws IOException {
        response.setStatus(errorType.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        jsonMapper.writeValue(response.getWriter(), ApiResponse.error(errorType, data));
    }
}
