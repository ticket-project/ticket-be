package com.ticket.core.api.controller;

import com.ticket.core.api.controller.docs.AuthControllerDocs;
import com.ticket.core.domain.auth.usecase.*;
import com.ticket.core.domain.member.MemberPrincipal;
import com.ticket.core.support.exception.AuthException;
import com.ticket.core.support.exception.ErrorType;
import com.ticket.core.support.response.ApiResponse;
import com.ticket.core.support.util.CookieUtils;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController implements AuthControllerDocs {

    private final RegisterMemberUseCase registerMemberUseCase;
    private final LoginUseCase loginUseCase;
    private final RefreshAuthTokenUseCase refreshAuthTokenUseCase;
    private final ExchangeOAuth2TokenUseCase exchangeOAuth2TokenUseCase;
    private final GetSocialLoginUrlsUseCase getSocialLoginUrlsUseCase;
    private final LogoutUseCase logoutUseCase;

    @Override
    @PostMapping("/signup")
    public ApiResponse<RegisterMemberUseCase.Output> signUp(@Valid @RequestBody final RegisterMemberUseCase.Input input) {
        return ApiResponse.success(registerMemberUseCase.execute(input));
    }

    @Override
    @PostMapping("/login")
    public ApiResponse<LoginUseCase.Output> login(
            @Valid @RequestBody final LoginUseCase.Input input,
            final HttpServletResponse response
    ) {
        return ApiResponse.success(loginUseCase.execute(input, response));
    }

    @Override
    @PostMapping("/refresh")
    public ApiResponse<RefreshAuthTokenUseCase.Output> refresh(
            @CookieValue(name = CookieUtils.REFRESH_TOKEN_COOKIE_NAME, required = false) final String refreshToken,
            final HttpServletResponse response
    ) {
        if (refreshToken == null) {
            throw new AuthException(ErrorType.AUTHENTICATION_ERROR);
        }
        final RefreshAuthTokenUseCase.Input input = new RefreshAuthTokenUseCase.Input(refreshToken);
        return ApiResponse.success(refreshAuthTokenUseCase.execute(input, response));
    }

    @Override
    @PostMapping("/oauth2/token")
    public ApiResponse<ExchangeOAuth2TokenUseCase.Output> exchangeOAuth2Token(
            @Valid @RequestBody final ExchangeOAuth2TokenUseCase.Input input,
            final HttpServletResponse response
    ) {
        return ApiResponse.success(exchangeOAuth2TokenUseCase.execute(input, response));
    }

    @Override
    @GetMapping("/social/urls")
    public ApiResponse<Map<String, String>> getSocialLoginUrls() {
        final String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        final GetSocialLoginUrlsUseCase.Input input = new GetSocialLoginUrlsUseCase.Input(baseUrl);
        return ApiResponse.success(getSocialLoginUrlsUseCase.execute(input).urls());
    }

    @Override
    @PostMapping("/logout")
    public ApiResponse<LogoutUseCase.Output> logout(
            @AuthenticationPrincipal final MemberPrincipal principal,
            @CookieValue(name = CookieUtils.REFRESH_TOKEN_COOKIE_NAME, required = false) final String refreshToken,
            final HttpServletResponse response
    ) {
        if (refreshToken == null) {
            throw new AuthException(ErrorType.AUTHENTICATION_ERROR);
        }
        final LogoutUseCase.Input input = new LogoutUseCase.Input(principal.getMemberId(), refreshToken);
        return ApiResponse.success(logoutUseCase.execute(input, response));
    }
}
