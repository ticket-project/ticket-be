package com.ticket.core.domain.auth.command;

import com.ticket.core.domain.auth.OAuth2AuthCodeService;
import com.ticket.core.domain.auth.token.AuthTokenManager;
import com.ticket.core.domain.auth.token.IssuedAuthTokens;
import com.ticket.core.domain.member.model.Member;
import com.ticket.core.domain.member.query.MemberFinder;
import com.ticket.core.support.exception.AuthException;
import com.ticket.core.support.exception.ErrorType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    private AuthTokenManager authTokenManager;

    @InjectMocks
    private ExchangeOAuth2TokenUseCase useCase;

    @Test
    void valid_code_issues_tokens() {
        Member member = mock(Member.class);
        IssuedAuthTokens response = new IssuedAuthTokens("access", "refresh", "Bearer", 1800L, 7L);

        when(oAuth2AuthCodeService.consumeCode("oauth-code")).thenReturn(Optional.of(7L));
        when(memberFinder.findActiveMemberById(7L)).thenReturn(member);
        when(authTokenManager.issueTokens(member)).thenReturn(response);

        ExchangeOAuth2TokenUseCase.Result result =
                useCase.execute(new ExchangeOAuth2TokenUseCase.Input("oauth-code"));
        ExchangeOAuth2TokenUseCase.Output output = result.output();

        assertThat(output.accessToken()).isEqualTo(response.accessToken());
        assertThat(output.tokenType()).isEqualTo(response.tokenType());
        assertThat(output.expiresIn()).isEqualTo(response.expiresIn());
        assertThat(output.memberId()).isEqualTo(response.memberId());
        assertThat(result.refreshToken()).isEqualTo("refresh");
        verify(oAuth2AuthCodeService).consumeCode("oauth-code");
        verify(memberFinder).findActiveMemberById(7L);
        verify(authTokenManager).issueTokens(member);
    }

    @Test
    void invalid_code_throws_auth_exception() {
        when(oAuth2AuthCodeService.consumeCode("invalid")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new ExchangeOAuth2TokenUseCase.Input("invalid")))
                .isInstanceOf(AuthException.class)
                .satisfies(exception -> assertThat(((AuthException) exception).getErrorType()).isEqualTo(ErrorType.AUTHENTICATION_ERROR));
    }
}
