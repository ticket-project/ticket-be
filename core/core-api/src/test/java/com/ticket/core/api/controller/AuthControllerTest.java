package com.ticket.core.api.controller;

import com.ticket.core.config.security.SocialLoginBaseUrlResolver;
import com.ticket.core.domain.auth.usecase.ExchangeOAuth2TokenUseCase;
import com.ticket.core.domain.auth.usecase.GetSocialLoginUrlsUseCase;
import com.ticket.core.domain.auth.usecase.LoginUseCase;
import com.ticket.core.domain.auth.usecase.LogoutUseCase;
import com.ticket.core.domain.auth.usecase.RefreshAuthTokenUseCase;
import com.ticket.core.domain.auth.usecase.RegisterMemberUseCase;
import com.ticket.core.support.response.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private RegisterMemberUseCase registerMemberUseCase;

    @Mock
    private LoginUseCase loginUseCase;

    @Mock
    private RefreshAuthTokenUseCase refreshAuthTokenUseCase;

    @Mock
    private ExchangeOAuth2TokenUseCase exchangeOAuth2TokenUseCase;

    @Mock
    private GetSocialLoginUrlsUseCase getSocialLoginUrlsUseCase;

    @Mock
    private LogoutUseCase logoutUseCase;

    @Mock
    private SocialLoginBaseUrlResolver socialLoginBaseUrlResolver;

    private AuthController authController;

    @BeforeEach
    void setUp() {
        authController = new AuthController(
                registerMemberUseCase,
                loginUseCase,
                refreshAuthTokenUseCase,
                exchangeOAuth2TokenUseCase,
                getSocialLoginUrlsUseCase,
                socialLoginBaseUrlResolver,
                logoutUseCase
        );
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void 로컬_프론트_origin이면_localhost_8080_기준_소셜로그인_url을_반환한다() {
        MockHttpServletRequest request = request("https", "api.oneticket.site", 443);
        request.addHeader("Origin", "http://localhost:3000");
        Map<String, String> urls = Map.of("google", "http://localhost:8080/api/v1/auth/oauth2/authorize/google");
        GetSocialLoginUrlsUseCase.Output output = new GetSocialLoginUrlsUseCase.Output(urls);
        when(socialLoginBaseUrlResolver.resolve(any(), any(), any()))
                .thenReturn("http://localhost:8080");
        when(getSocialLoginUrlsUseCase.execute(any()))
                .thenReturn(output);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        ApiResponse<Map<String, String>> response = authController.getSocialLoginUrls();

        assertThat(response.getData()).isEqualTo(urls);
        verify(socialLoginBaseUrlResolver).resolve("http://localhost:3000", null, "https://api.oneticket.site");
    }

    @Test
    void 운영_프론트_origin이면_oneticket_site_기준_소셜로그인_url을_반환한다() {
        MockHttpServletRequest request = request("https", "api.oneticket.site", 443);
        request.addHeader("Origin", "https://oneticket.site");
        Map<String, String> urls = Map.of("google", "https://oneticket.site/api/v1/auth/oauth2/authorize/google");
        GetSocialLoginUrlsUseCase.Output output = new GetSocialLoginUrlsUseCase.Output(urls);
        when(socialLoginBaseUrlResolver.resolve(any(), any(), any()))
                .thenReturn("https://oneticket.site");
        when(getSocialLoginUrlsUseCase.execute(any()))
                .thenReturn(output);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        ApiResponse<Map<String, String>> response = authController.getSocialLoginUrls();

        assertThat(response.getData()).isEqualTo(urls);
        verify(socialLoginBaseUrlResolver).resolve("https://oneticket.site", null, "https://api.oneticket.site");
    }

    private MockHttpServletRequest request(
            final String scheme,
            final String serverName,
            final int serverPort
    ) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme(scheme);
        request.setServerName(serverName);
        request.setServerPort(serverPort);
        request.setRequestURI("/api/v1/auth/social/urls");
        return request;
    }
}
