package com.ticket.core.api.controller.docs;

import com.ticket.core.api.controller.request.AddMemberRequest;
import com.ticket.core.api.controller.request.LoginMemberRequest;
import com.ticket.core.api.controller.request.OAuth2TokenExchangeRequest;
import com.ticket.core.api.controller.request.RefreshTokenRequest;
import com.ticket.core.api.controller.response.AuthLoginResponse;
import com.ticket.core.domain.member.MemberPrincipal;
import com.ticket.core.support.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Map;

@Tag(name = "인증(Auth)", description = "회원가입, 로그인, 토큰 갱신, 로그아웃 및 소셜 로그인 API")
public interface AuthControllerDocs {

    @Operation(summary = "회원가입", description = "이메일(로그인 아이디), 비밀번호, 이름으로 회원가입합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "회원가입 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청값이 올바르지 않음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 가입된 이메일")
    })
    ApiResponse<Long> signUp(AddMemberRequest request);

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하고 Access Token + Refresh Token을 발급합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호 불일치")
    })
    ApiResponse<AuthLoginResponse> login(LoginMemberRequest request);

    @Operation(summary = "토큰 갱신", description = "Refresh Token을 사용하여 새 Access Token과 Refresh Token을 발급합니다. (Token Rotation)")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "유효하지 않거나 만료된 리프레시 토큰")
    })
    ApiResponse<AuthLoginResponse> refresh(RefreshTokenRequest request);

    @Operation(summary = "OAuth2 토큰 교환", description = "OAuth2 로그인 성공 후 발급된 1회용 인증 코드를 Access Token + Refresh Token으로 교환합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토큰 교환 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "유효하지 않거나 만료된 인증 코드")
    })
    ApiResponse<AuthLoginResponse> exchangeOAuth2Token(OAuth2TokenExchangeRequest request);

    @Operation(summary = "소셜 로그인 URL 조회", description = "Google, Kakao 소셜 로그인 진입 URL을 반환합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    ApiResponse<Map<String, String>> getSocialLoginUrls();

    @Operation(summary = "로그아웃", description = "Refresh Token을 무효화합니다. Access Token은 짧은 만료 시간으로 자연 무효화됩니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 필요")
    })
    ApiResponse<Void> logout(MemberPrincipal principal, RefreshTokenRequest request);
}
