package com.ticket.core.domain.auth.usecase;

import com.ticket.core.api.controller.response.AuthLoginResponse;
import com.ticket.core.domain.auth.AuthService;
import com.ticket.core.domain.auth.token.AuthTokenApplicationService;
import com.ticket.core.domain.member.Member;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

    @Mock
    private AuthService authService;

    @Mock
    private AuthTokenApplicationService authTokenApplicationService;

    @InjectMocks
    private LoginUseCase useCase;

    @Test
    void 로그인에_성공하면_토큰을_발급한다() {
        Member member = mock(Member.class);
        AuthLoginResponse response = new AuthLoginResponse("access", "Bearer", 1800L, 1L);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();

        when(authService.login("user@example.com", "password")).thenReturn(member);
        when(authTokenApplicationService.issueTokens(member, servletResponse)).thenReturn(response);

        LoginUseCase.Output output = useCase.execute(new LoginUseCase.Input("user@example.com", "password"), servletResponse);

        assertThat(output.authLoginResponse()).isEqualTo(response);
        verify(authService).login("user@example.com", "password");
        verify(authTokenApplicationService).issueTokens(member, servletResponse);
    }
}
