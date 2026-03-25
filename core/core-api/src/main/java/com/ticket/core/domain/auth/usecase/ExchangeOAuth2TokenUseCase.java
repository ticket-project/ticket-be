package com.ticket.core.domain.auth.usecase;

import com.ticket.core.api.controller.response.AuthLoginResponse;
import com.ticket.core.domain.auth.OAuth2AuthCodeService;
import com.ticket.core.domain.auth.token.AuthTokenManager;
import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.MemberFinder;
import com.ticket.core.support.exception.AuthException;
import com.ticket.core.support.exception.ErrorType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExchangeOAuth2TokenUseCase {

    private final OAuth2AuthCodeService oAuth2AuthCodeService;
    private final MemberFinder memberFinder;
    private final AuthTokenManager authTokenManager;

    public record Input(
            @Schema(description = "서버에서 발급한 1회용 인증 코드", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotBlank
            String code
    ) {}

    public record Output(
            @Schema(description = "액세스 토큰(JWT)") String accessToken,
            @Schema(description = "토큰 타입", example = "Bearer") String tokenType,
            @Schema(description = "액세스 토큰 만료 시간(초)", example = "1800") long expiresIn,
            @Schema(description = "회원 ID", example = "1") Long memberId
    ) {}

    public Output execute(final Input input, final HttpServletResponse response) {
        final Long memberId = oAuth2AuthCodeService.consumeCode(input.code())
                .orElseThrow(() -> new AuthException(ErrorType.AUTHENTICATION_ERROR, "유효하지 않거나 만료된 인증 코드입니다."));
        final Member member = memberFinder.findActiveMemberById(memberId);
        final AuthLoginResponse result = authTokenManager.issueTokens(member, response);
        return new Output(result.accessToken(), result.tokenType(), result.expiresIn(), result.memberId());
    }
}
