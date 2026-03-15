package com.ticket.core.domain.auth.usecase;

import com.ticket.core.domain.auth.token.AuthTokenApplicationService;
import com.ticket.core.domain.auth.token.RefreshTokenService;
import com.ticket.core.support.exception.AuthException;
import com.ticket.core.support.exception.ErrorType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogoutUseCaseTest {

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private AuthTokenApplicationService authTokenApplicationService;

    @InjectMocks
    private LogoutUseCase useCase;

    @Test
    void 본인_토큰이면_쿠키를_정리한다() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(refreshTokenService.revokeIfOwned("refresh-token", 1L)).thenReturn(true);

        useCase.execute(new LogoutUseCase.Input(1L, "refresh-token"), response);

        verify(refreshTokenService).revokeIfOwned("refresh-token", 1L);
        verify(authTokenApplicationService).clearRefreshTokenCookie(response);
    }

    @Test
    void 다른_회원의_토큰이면_인가_예외를_던진다() {
        when(refreshTokenService.revokeIfOwned("refresh-token", 1L)).thenReturn(false);
        when(refreshTokenService.validateWithoutConsume("refresh-token")).thenReturn(Optional.of(2L));

        assertThatThrownBy(() -> useCase.execute(new LogoutUseCase.Input(1L, "refresh-token"), new MockHttpServletResponse()))
                .isInstanceOf(AuthException.class)
                .satisfies(exception -> assertThat(((AuthException) exception).getErrorType()).isEqualTo(ErrorType.AUTHORIZATION_ERROR));

        verifyNoInteractions(authTokenApplicationService);
    }

    @Test
    void 토큰_소유자를_확인할_수_없으면_인증_예외를_던진다() {
        when(refreshTokenService.revokeIfOwned("refresh-token", 1L)).thenReturn(false);
        when(refreshTokenService.validateWithoutConsume("refresh-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new LogoutUseCase.Input(1L, "refresh-token"), new MockHttpServletResponse()))
                .isInstanceOf(AuthException.class)
                .satisfies(exception -> assertThat(((AuthException) exception).getErrorType()).isEqualTo(ErrorType.AUTHENTICATION_ERROR));
    }
}
