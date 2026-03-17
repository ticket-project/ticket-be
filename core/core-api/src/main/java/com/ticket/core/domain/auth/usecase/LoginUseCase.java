package com.ticket.core.domain.auth.usecase;

import com.ticket.core.api.controller.response.AuthLoginResponse;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.ticket.core.domain.auth.AuthService;
import com.ticket.core.domain.auth.token.AuthTokenApplicationService;
import com.ticket.core.domain.member.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginUseCase {

    private final AuthService authService;
    private final AuthTokenApplicationService authTokenApplicationService;

    public record Input(
            @Schema(description = "로그인 아이디(이메일)", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
            @JsonAlias({"id", "loginId"})
            @NotBlank
            String email,

            @Schema(description = "비밀번호", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotBlank
            String password
    ) {}
    public record Output(@Schema(description = "액세스 토큰(JWT)") String accessToken,
                         @Schema(description = "토큰 타입", example = "Bearer") String tokenType,
                         @Schema(description = "액세스 토큰 만료 시간(초)", example = "1800") long expiresIn,
                         @Schema(description = "회원 ID", example = "1") Long memberId) {}

    public Output execute(final Input input, final HttpServletResponse response) {
        final Member member = authService.login(input.email(), input.password());
        final AuthLoginResponse result = authTokenApplicationService.issueTokens(member, response);
        return new Output(result.accessToken(), result.tokenType(), result.expiresIn(), result.memberId());
    }
}
