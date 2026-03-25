package com.ticket.core.domain.auth.token;

import com.ticket.core.api.controller.response.AuthLoginResponse;
import com.ticket.core.config.security.JwtProperties;
import com.ticket.core.config.security.JwtTokenService;
import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.vo.Email;
import com.ticket.core.domain.member.vo.EncodedPassword;
import com.ticket.core.enums.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
class AuthTokenManagerTest {

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthTokenManager authTokenManager;

    @Test
    void issue_tokens_sets_refresh_cookie() {
        Member member = createMember(7L);
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(jwtTokenService.createAccessToken(any())).thenReturn("access-token");
        when(jwtTokenService.getAccessTokenExpirationSeconds()).thenReturn(1800L);
        when(jwtProperties.getRefreshTokenExpirationSeconds()).thenReturn(1209600L);
        when(refreshTokenService.createRefreshToken(7L, 1209600L)).thenReturn("refresh-token");

        AuthLoginResponse result = authTokenManager.issueTokens(member, response);

        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.tokenType()).isEqualTo("Bearer");
        assertThat(result.expiresIn()).isEqualTo(1800L);
        assertThat(result.memberId()).isEqualTo(7L);
        assertThat(response.getHeaders("Set-Cookie"))
                .singleElement()
                .asString()
                .contains("refresh_token=refresh-token")
                .contains("HttpOnly")
                .contains("Secure")
                .contains("SameSite=None")
                .contains("Path=/api/v1/auth")
                .contains("Max-Age=1209600");
    }

    @Test
    void rotate_tokens_replaces_refresh_cookie() {
        Member member = createMember(7L);
        MockHttpServletResponse response = new MockHttpServletResponse();
        AuthRefreshToken refreshToken = AuthRefreshToken.from("old-refresh");
        when(refreshTokenService.rotate(refreshToken, 7L, 1209600L)).thenReturn("new-refresh");
        when(jwtProperties.getRefreshTokenExpirationSeconds()).thenReturn(1209600L);
        when(jwtTokenService.createAccessToken(any())).thenReturn("new-access");
        when(jwtTokenService.getAccessTokenExpirationSeconds()).thenReturn(1800L);

        AuthLoginResponse result = authTokenManager.rotateTokens(member, refreshToken, response);

        verify(refreshTokenService).rotate(refreshToken, 7L, 1209600L);
        assertThat(result.accessToken()).isEqualTo("new-access");
        assertThat(result.memberId()).isEqualTo(7L);
        assertThat(response.getHeaders("Set-Cookie"))
                .singleElement()
                .asString()
                .contains("refresh_token=new-refresh")
                .contains("Max-Age=1209600");
    }

    @Test
    void clear_refresh_token_cookie_sets_zero_max_age() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        authTokenManager.clearRefreshTokenCookie(response);

        assertThat(response.getHeaders("Set-Cookie"))
                .singleElement()
                .asString()
                .contains("refresh_token=")
                .contains("Max-Age=0");
    }

    private Member createMember(final Long id) {
        Member member = new Member(Email.create("user@example.com"), EncodedPassword.create("encoded"), "user", Role.MEMBER);
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }
}
