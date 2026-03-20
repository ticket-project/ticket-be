package com.ticket.core.api.controller;

import com.ticket.core.config.LoginMemberArgumentResolver;
import com.ticket.core.domain.auth.usecase.ExchangeOAuth2TokenUseCase;
import com.ticket.core.domain.auth.usecase.GetSocialLoginUrlsUseCase;
import com.ticket.core.domain.auth.usecase.LoginUseCase;
import com.ticket.core.domain.auth.usecase.LogoutUseCase;
import com.ticket.core.domain.auth.usecase.RefreshAuthTokenUseCase;
import com.ticket.core.domain.auth.usecase.RegisterMemberUseCase;
import com.ticket.core.domain.member.MemberPrincipal;
import com.ticket.core.enums.Role;
import com.ticket.core.support.ApiControllerAdvice;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("NonAsciiCharacters")
class AuthControllerContractTest {

    private MockMvc mockMvc;

    private final LoginUseCase loginUseCase = Mockito.mock(LoginUseCase.class);
    private final RefreshAuthTokenUseCase refreshAuthTokenUseCase = Mockito.mock(RefreshAuthTokenUseCase.class);
    private final LogoutUseCase logoutUseCase = Mockito.mock(LogoutUseCase.class);

    @BeforeEach
    void setUp() {
        AuthController controller = new AuthController(
                Mockito.mock(RegisterMemberUseCase.class),
                loginUseCase,
                refreshAuthTokenUseCase,
                Mockito.mock(ExchangeOAuth2TokenUseCase.class),
                Mockito.mock(GetSocialLoginUrlsUseCase.class),
                logoutUseCase
        );
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new LoginMemberArgumentResolver())
                .setControllerAdvice(new ApiControllerAdvice())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void 로그인_API는_성공_응답_계약을_유지한다() throws Exception {
        when(loginUseCase.execute(any(LoginUseCase.Input.class), any()))
                .thenReturn(new LoginUseCase.Output("access-token", "Bearer", 1800L, 1L));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "password": "password"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.memberId").value(1))
                .andExpect(jsonPath("$.error").isEmpty());
    }

    @Test
    void 리프레시_API는_쿠키가_없으면_인증_오류_계약을_유지한다() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.result").value("ERROR"))
                .andExpect(jsonPath("$.error.code").value("E1000"));
    }

    @Test
    void 로그아웃_API는_성공_응답_계약을_유지한다() throws Exception {
        MemberPrincipal principal = new MemberPrincipal(1L, Role.MEMBER);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
        when(logoutUseCase.execute(any(LogoutUseCase.Input.class), any()))
                .thenReturn(new LogoutUseCase.Output());

        mockMvc.perform(post("/api/v1/auth/logout")
                        .cookie(new MockCookie("refresh_token", "refresh-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.error").isEmpty());
    }
}
