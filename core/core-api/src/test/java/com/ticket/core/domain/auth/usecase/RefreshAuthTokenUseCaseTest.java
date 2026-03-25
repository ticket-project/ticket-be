package com.ticket.core.domain.auth.usecase;

import com.ticket.core.api.controller.response.AuthLoginResponse;
import com.ticket.core.domain.auth.token.AuthTokenManager;
import com.ticket.core.domain.auth.token.RefreshTokenService;
import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.MemberFinder;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NonAsciiCharacters")
class RefreshAuthTokenUseCaseTest {

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private MemberFinder memberFinder;

    @Mock
    private AuthTokenManager authTokenManager;

    @InjectMocks
    private RefreshAuthTokenUseCase useCase;

    @Test
    void valid_refresh_token_rotates_tokens() {
        Member member = mock(Member.class);
        AuthLoginResponse response = new AuthLoginResponse("access", "Bearer", 1800L, 3L);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();

        AuthRefreshToken refreshToken = AuthRefreshToken.from("refresh-token");
        when(refreshTokenService.validate(refreshToken)).thenReturn(Optional.of(3L));
        when(memberFinder.findActiveMemberById(3L)).thenReturn(member);
        when(authTokenManager.rotateTokens(member, refreshToken, servletResponse)).thenReturn(response);

        RefreshAuthTokenUseCase.Output output =
                useCase.execute(new RefreshAuthTokenUseCase.Input(refreshToken), servletResponse);

        assertThat(output.accessToken()).isEqualTo(response.accessToken());
        assertThat(output.tokenType()).isEqualTo(response.tokenType());
        assertThat(output.expiresIn()).isEqualTo(response.expiresIn());
        assertThat(output.memberId()).isEqualTo(response.memberId());
        verify(refreshTokenService).validate(refreshToken);
        verify(memberFinder).findActiveMemberById(3L);
        verify(authTokenManager).rotateTokens(member, refreshToken, servletResponse);
    }

    @Test
    void invalid_refresh_token_throws_auth_exception() {
        AuthRefreshToken refreshToken = AuthRefreshToken.from("refresh-token");
        when(refreshTokenService.validate(refreshToken)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new RefreshAuthTokenUseCase.Input(refreshToken), new MockHttpServletResponse()))
                .isInstanceOf(AuthException.class)
                .satisfies(exception -> assertThat(((AuthException) exception).getErrorType()).isEqualTo(ErrorType.AUTHENTICATION_ERROR));
    }
}
