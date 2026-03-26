package com.ticket.core.domain.auth.command;

import com.ticket.core.domain.auth.token.AuthRefreshToken;
import com.ticket.core.domain.auth.token.AuthTokenManager;
import com.ticket.core.domain.auth.token.IssuedAuthTokens;
import com.ticket.core.domain.auth.token.RefreshTokenService;
import com.ticket.core.domain.member.model.Member;
import com.ticket.core.domain.member.query.MemberFinder;
import com.ticket.core.support.exception.AuthException;
import com.ticket.core.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshAuthTokenUseCase {

    private final RefreshTokenService refreshTokenService;
    private final MemberFinder memberFinder;
    private final AuthTokenManager authTokenManager;

    public record Input(AuthRefreshToken refreshToken) {}

    public record Output(
            String accessToken,
            String tokenType,
            long expiresIn,
            Long memberId
    ) {}

    public record Result(Output output, String refreshToken) {}

    public Result execute(final Input input) {
        final Long memberId = refreshTokenService.validate(input.refreshToken())
                .orElseThrow(() -> new AuthException(ErrorType.AUTHENTICATION_ERROR, "유효하지 않거나 만료된 리프레시 토큰입니다."));
        final Member member = memberFinder.findActiveMemberById(memberId);
        final IssuedAuthTokens result = authTokenManager.rotateTokens(member, input.refreshToken());
        return new Result(
                new Output(result.accessToken(), result.tokenType(), result.expiresIn(), result.memberId()),
                result.refreshToken()
        );
    }
}
