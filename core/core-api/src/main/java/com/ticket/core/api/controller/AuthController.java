package com.ticket.core.api.controller;

import com.ticket.core.api.controller.docs.AuthControllerDocs;
import com.ticket.core.config.security.SocialLoginBaseUrlResolver;
import com.ticket.core.domain.auth.usecase.*;
import com.ticket.core.domain.member.MemberPrincipal;
import com.ticket.core.support.exception.AuthException;
import com.ticket.core.support.exception.ErrorType;
import com.ticket.core.support.response.ApiResponse;
import com.ticket.core.support.util.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UriComponentsBuilder;

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
    private final SocialLoginBaseUrlResolver socialLoginBaseUrlResolver;
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
        final HttpServletRequest request = currentRequest();
        final String origin = request.getHeader("Origin");
        final String referer = request.getHeader("Referer");
        final String requestBaseUrl = requestBaseUrl(request);
        final String baseUrl = socialLoginBaseUrlResolver.resolve(origin, referer, requestBaseUrl);
        final GetSocialLoginUrlsUseCase.Input input = new GetSocialLoginUrlsUseCase.Input(baseUrl);
        return ApiResponse.success(getSocialLoginUrlsUseCase.execute(input).urls());
    }

    private HttpServletRequest currentRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
    }

    private String requestBaseUrl(final HttpServletRequest request) {
        UriComponentsBuilder builder = UriComponentsBuilder.newInstance()
                .scheme(request.getScheme())
                .host(request.getServerName());
        final Integer port = serverPort(request);
        if (port != null) {
            builder.port(port);
        }
        return builder.build().toUriString();
    }

    private Integer serverPort(final HttpServletRequest request) {
        if (isDefaultPort(request)) {
            return null;
        }
        return request.getServerPort();
    }

    private boolean isDefaultPort(final HttpServletRequest request) {
        return "http".equalsIgnoreCase(request.getScheme()) && request.getServerPort() == 80
                || "https".equalsIgnoreCase(request.getScheme()) && request.getServerPort() == 443;
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
