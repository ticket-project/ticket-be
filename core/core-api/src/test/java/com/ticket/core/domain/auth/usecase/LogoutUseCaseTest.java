package com.ticket.core.domain.auth.usecase;

import com.ticket.core.domain.auth.token.AuthTokenManager;
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
@SuppressWarnings("NonAsciiCharacters")
class LogoutUseCaseTest {

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private AuthTokenManager authTokenManager;

    @InjectMocks
    private LogoutUseCase useCase;

    @Test
    void owned_token_clears_cookie() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        AuthRefreshToken refreshToken = AuthRefreshToken.from("refresh-token");
        when(refreshTokenService.revokeIfOwned(refreshToken, 1L)).thenReturn(true);

        useCase.execute(new LogoutUseCase.Input(1L, refreshToken), response);

        verify(refreshTokenService).revokeIfOwned(refreshToken, 1L);
        verify(authTokenManager).clearRefreshTokenCookie(response);
    }

    @Test
    void foreign_token_throws_authorization_exception() {
        AuthRefreshToken refreshToken = AuthRefreshToken.from("refresh-token");
        when(refreshTokenService.revokeIfOwned(refreshToken, 1L)).thenReturn(false);
        when(refreshTokenService.validateWithoutConsume(refreshToken)).thenReturn(Optional.of(2L));

        assertThatThrownBy(() -> useCase.execute(new LogoutUseCase.Input(1L, refreshToken), new MockHttpServletResponse()))
                .isInstanceOf(AuthException.class)
                .satisfies(exception -> assertThat(((AuthException) exception).getErrorType()).isEqualTo(ErrorType.AUTHORIZATION_ERROR));

        verifyNoInteractions(authTokenManager);
    }

    @Test
    void missing_owner_check_result_throws_authentication_exception() {
        AuthRefreshToken refreshToken = AuthRefreshToken.from("refresh-token");
        when(refreshTokenService.revokeIfOwned(refreshToken, 1L)).thenReturn(false);
        when(refreshTokenService.validateWithoutConsume(refreshToken)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new LogoutUseCase.Input(1L, refreshToken), new MockHttpServletResponse()))
                .isInstanceOf(AuthException.class)
                .satisfies(exception -> assertThat(((AuthException) exception).getErrorType()).isEqualTo(ErrorType.AUTHENTICATION_ERROR));
    }
}
