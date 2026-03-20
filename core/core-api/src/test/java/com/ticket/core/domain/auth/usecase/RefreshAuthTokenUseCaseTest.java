package com.ticket.core.domain.auth.usecase;

import com.ticket.core.api.controller.response.AuthLoginResponse;
import com.ticket.core.domain.auth.token.AuthTokenApplicationService;
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
    private AuthTokenApplicationService authTokenApplicationService;

    @InjectMocks
    private RefreshAuthTokenUseCase useCase;

    @Test
    void 리프레시_토큰이_유효하면_토큰을_재발급한다() {
        //given
        Member member = mock(Member.class);
        AuthLoginResponse response = new AuthLoginResponse("access", "Bearer", 1800L, 3L);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();

        when(refreshTokenService.validate("refresh-token")).thenReturn(Optional.of(3L));
        when(memberFinder.findActiveMemberById(3L)).thenReturn(member);
        when(authTokenApplicationService.rotateTokens(member, "refresh-token", servletResponse)).thenReturn(response);

        //when
        RefreshAuthTokenUseCase.Output output =
                useCase.execute(new RefreshAuthTokenUseCase.Input(AuthRefreshToken.from("refresh-token")), servletResponse);

        //then
        assertThat(output.accessToken()).isEqualTo(response.accessToken());
        assertThat(output.tokenType()).isEqualTo(response.tokenType());
        assertThat(output.expiresIn()).isEqualTo(response.expiresIn());
        assertThat(output.memberId()).isEqualTo(response.memberId());
        verify(refreshTokenService).validate("refresh-token");
        verify(memberFinder).findActiveMemberById(3L);
        verify(authTokenApplicationService).rotateTokens(member, "refresh-token", servletResponse);
    }

    @Test
    void 리프레시_토큰이_유효하지_않으면_인증_예외를_던진다() {
        //given
        when(refreshTokenService.validate("refresh-token")).thenReturn(Optional.empty());

        //when
        //then
        assertThatThrownBy(() -> useCase.execute(new RefreshAuthTokenUseCase.Input(AuthRefreshToken.from("refresh-token")), new MockHttpServletResponse()))
                .isInstanceOf(AuthException.class)
                .satisfies(exception -> assertThat(((AuthException) exception).getErrorType()).isEqualTo(ErrorType.AUTHENTICATION_ERROR));
    }
}

