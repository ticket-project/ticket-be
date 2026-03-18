package com.ticket.core.domain.auth.usecase;

import com.ticket.core.api.controller.response.AuthLoginResponse;
import com.ticket.core.domain.auth.OAuth2AuthCodeService;
import com.ticket.core.domain.auth.token.AuthTokenApplicationService;
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
class ExchangeOAuth2TokenUseCaseTest {

    @Mock
    private OAuth2AuthCodeService oAuth2AuthCodeService;

    @Mock
    private MemberFinder memberFinder;

    @Mock
    private AuthTokenApplicationService authTokenApplicationService;

    @InjectMocks
    private ExchangeOAuth2TokenUseCase useCase;

    @Test
    void 인증코드가_유효하면_회원을_조회하고_토큰을_발급한다() {
        //given
        Member member = mock(Member.class);
        AuthLoginResponse response = new AuthLoginResponse("access", "Bearer", 1800L, 7L);
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();

        when(oAuth2AuthCodeService.consumeCode("oauth-code")).thenReturn(Optional.of(7L));
        when(memberFinder.findActiveMemberById(7L)).thenReturn(member);
        when(authTokenApplicationService.issueTokens(member, servletResponse)).thenReturn(response);

        //when
        ExchangeOAuth2TokenUseCase.Output output =
                useCase.execute(new ExchangeOAuth2TokenUseCase.Input("oauth-code"), servletResponse);

        //then
        assertThat(output.accessToken()).isEqualTo(response.accessToken());
        assertThat(output.tokenType()).isEqualTo(response.tokenType());
        assertThat(output.expiresIn()).isEqualTo(response.expiresIn());
        assertThat(output.memberId()).isEqualTo(response.memberId());
        verify(oAuth2AuthCodeService).consumeCode("oauth-code");
        verify(memberFinder).findActiveMemberById(7L);
        verify(authTokenApplicationService).issueTokens(member, servletResponse);
    }

    @Test
    void 인증코드가_유효하지_않으면_인증_예외를_던진다() {
        //given
        when(oAuth2AuthCodeService.consumeCode("invalid")).thenReturn(Optional.empty());

        //when
        //then
        assertThatThrownBy(() -> useCase.execute(new ExchangeOAuth2TokenUseCase.Input("invalid"), new MockHttpServletResponse()))
                .isInstanceOf(AuthException.class)
                .satisfies(exception -> assertThat(((AuthException) exception).getErrorType()).isEqualTo(ErrorType.AUTHENTICATION_ERROR));
    }
}

