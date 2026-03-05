package com.ticket.core.config.security;

import com.ticket.core.domain.auth.oauth2.CustomOAuth2UserService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableConfigurationProperties({JwtProperties.class, CorsProperties.class})
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            final HttpSecurity http,
            final JwtTokenService jwtTokenService,
            final CustomOAuth2UserService customOAuth2UserService,
            final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler,
            final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler,
            final RestAuthenticationEntryPoint restAuthenticationEntryPoint,
            final RestAccessDeniedHandler restAccessDeniedHandler
    ) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                        .accessDeniedHandler(restAccessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        // 정적 리소스 & 인프라
                        .requestMatchers("/", "/swagger-ui/**", "/api-docs/**", "/ws/**").permitAll()
                        // 인증 관련
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        // 읽기 전용 공개 API (GET만)
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/shows/**",
                                "/api/v1/performances/**",
                                "/api/v1/genres/**",
                                "/api/v1/meta/**"
                        ).permitAll()
                        // 나머지 전부 인증 필수
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(authorization ->
                                authorization.baseUri(OAuth2EndpointConstants.AUTHORIZATION_BASE_URI))
                        .redirectionEndpoint(redirection ->
                                redirection.baseUri(OAuth2EndpointConstants.CALLBACK_BASE_URI_PATTERN))
                        .userInfoEndpoint(userInfo ->
                                userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                        .failureHandler(oAuth2AuthenticationFailureHandler)
                );

        http.addFilterBefore(new JwtAuthenticationFilter(jwtTokenService), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(final CorsProperties corsProperties) {
        final CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(corsProperties.getAllowedOrigins());
        corsConfiguration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        corsConfiguration.setAllowedHeaders(List.of("*"));
        corsConfiguration.setExposedHeaders(List.of(HttpHeaders.AUTHORIZATION));
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setMaxAge(3600L);

        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", corsConfiguration);
        source.registerCorsConfiguration("/ws/**", corsConfiguration);
        return source;
    }
}
