package com.ticket.core.domain.auth.usecase;

import com.ticket.core.api.controller.response.AuthLoginResponse;
import com.ticket.core.domain.auth.AuthTokenApplicationService;
import com.ticket.core.domain.auth.RefreshTokenService;
import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.MemberFinder;
import com.ticket.core.support.exception.AuthException;
import com.ticket.core.support.exception.ErrorType;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshAuthTokenUseCase {

    private final RefreshTokenService refreshTokenService;
    private final MemberFinder memberFinder;
    private final AuthTokenApplicationService authTokenApplicationService;

    public record Input(String refreshToken) {}
    public record Output(AuthLoginResponse authLoginResponse) {}

    public Output execute(final Input input, final HttpServletResponse response) {
        final Long memberId = refreshTokenService.validate(input.refreshToken())
                .orElseThrow(() -> new AuthException(ErrorType.AUTHENTICATION_ERROR, "유효하지 않거나 만료된 리프레시 토큰입니다."));
        final Member member = memberFinder.findActiveMemberById(memberId);
        return new Output(authTokenApplicationService.rotateTokens(member, input.refreshToken(), response));
    }
}
