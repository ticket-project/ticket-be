package com.ticket.core.domain.auth.command;

import com.ticket.core.domain.auth.token.AuthRefreshToken;
import com.ticket.core.domain.auth.token.RefreshTokenService;
import com.ticket.core.support.exception.AuthException;
import com.ticket.core.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogoutUseCase {

    private final RefreshTokenService refreshTokenService;

    public record Input(Long memberId, AuthRefreshToken refreshToken) {}

    public record Output() {}

    public Output execute(final Input input) {
        final boolean revoked = refreshTokenService.revokeIfOwned(input.refreshToken(), input.memberId());
        if (!revoked) {
            final Long tokenOwnerId = refreshTokenService.validateWithoutConsume(input.refreshToken())
                    .orElseThrow(() -> new AuthException(ErrorType.AUTHENTICATION_ERROR, "유효하지 않은 리프레시 토큰입니다."));
            if (!tokenOwnerId.equals(input.memberId())) {
                throw new AuthException(ErrorType.AUTHORIZATION_ERROR, "본인 토큰만 무효화할 수 있습니다.");
            }

            throw new AuthException(ErrorType.AUTHENTICATION_ERROR, "이미 무효화된 토큰이거나 처리할 수 없는 상태입니다.");
        }

        return new Output();
    }
}
