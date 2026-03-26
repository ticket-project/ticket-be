package com.ticket.core.domain.auth.command;

import com.ticket.core.domain.auth.AuthService;
import com.ticket.core.domain.auth.token.AuthTokenManager;
import com.ticket.core.domain.auth.token.IssuedAuthTokens;
import com.ticket.core.domain.member.model.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginUseCase {

    private final AuthService authService;
    private final AuthTokenManager authTokenManager;

    public record Input(
            String email,
            String password
    ) {}
    public record Output(String accessToken,
                         String tokenType,
                         long expiresIn,
                         Long memberId) {}
    public record Result(Output output, String refreshToken) {}

    public Result execute(final Input input) {
        final Member member = authService.login(input.email(), input.password());
        final IssuedAuthTokens result = authTokenManager.issueTokens(member);
        return new Result(
                new Output(result.accessToken(), result.tokenType(), result.expiresIn(), result.memberId()),
                result.refreshToken()
        );
    }
}
