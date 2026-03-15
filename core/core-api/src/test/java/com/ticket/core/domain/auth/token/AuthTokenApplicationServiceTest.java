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
class AuthTokenApplicationServiceTest {

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthTokenApplicationService authTokenApplicationService;

    @Test
    void 토큰_발급시_응답과_리프레시토큰_쿠키를_함께_반환한다() {
        //given
        Member member = createMember(7L);
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(jwtTokenService.createAccessToken(any())).thenReturn("access-token");
        when(jwtTokenService.getAccessTokenExpirationSeconds()).thenReturn(1800L);
        when(jwtProperties.getRefreshTokenExpirationSeconds()).thenReturn(1209600L);
        when(refreshTokenService.createRefreshToken(7L, 1209600L)).thenReturn("refresh-token");

        //when
        AuthLoginResponse result = authTokenApplicationService.issueTokens(member, response);

        //then
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
    void 토큰_재발급시_기존_리프레시토큰을_교체하고_새_쿠키를_내린다() {
        //given
        Member member = createMember(7L);
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(refreshTokenService.rotate("old-refresh", 7L, 1209600L)).thenReturn("new-refresh");
        when(jwtProperties.getRefreshTokenExpirationSeconds()).thenReturn(1209600L);
        when(jwtTokenService.createAccessToken(any())).thenReturn("new-access");
        when(jwtTokenService.getAccessTokenExpirationSeconds()).thenReturn(1800L);

        //when
        AuthLoginResponse result = authTokenApplicationService.rotateTokens(member, "old-refresh", response);

        //then
        verify(refreshTokenService).rotate("old-refresh", 7L, 1209600L);
        assertThat(result.accessToken()).isEqualTo("new-access");
        assertThat(result.memberId()).isEqualTo(7L);
        assertThat(response.getHeaders("Set-Cookie"))
                .singleElement()
                .asString()
                .contains("refresh_token=new-refresh")
                .contains("Max-Age=1209600");
    }

    @Test
    void 리프레시토큰_쿠키_삭제시_만료시간_0으로_설정한다() {
        //given
        MockHttpServletResponse response = new MockHttpServletResponse();

        //when
        authTokenApplicationService.clearRefreshTokenCookie(response);

        //then
        assertThat(response.getHeaders("Set-Cookie"))
                .singleElement()
                .asString()
                .contains("refresh_token=")
                .contains("Max-Age=0");
    }

    private Member createMember(final Long id) {
        Member member = new Member(Email.create("user@example.com"), EncodedPassword.create("encoded"), "사용자", Role.MEMBER);
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }
}

