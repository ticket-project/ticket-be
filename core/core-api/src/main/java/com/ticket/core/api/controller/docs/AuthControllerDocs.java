package com.ticket.core.api.controller.docs;

import com.ticket.core.api.controller.request.AddMemberRequest;
import com.ticket.core.api.controller.request.LoginMemberRequest;
import com.ticket.core.api.controller.response.AuthLoginResponse;
import com.ticket.core.support.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;

@Tag(name = "인증(Auth)", description = "회원가입, 로그인, 로그아웃 및 소셜 로그인 URL 조회 API")
public interface AuthControllerDocs {

    @Operation(
            summary = "회원가입",
            description = "이메일(로그인 아이디), 비밀번호, 이름으로 회원가입합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "회원가입 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청값이 올바르지 않음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 가입된 이메일")
    })
    ApiResponse<Long> signUp(AddMemberRequest request);

    @Operation(
            summary = "로그인",
            description = "이메일(로그인 아이디)과 비밀번호로 로그인하고 Access Token(JWT)을 발급합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "요청값이 올바르지 않음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호 불일치")
    })
    ApiResponse<AuthLoginResponse> login(LoginMemberRequest request);

    @Operation(
            summary = "소셜 로그인 URL 조회",
            description = "Google, Kakao 소셜 로그인 진입 URL을 반환합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공")
    })
    ApiResponse<Map<String, String>> getSocialLoginUrls();

    @Operation(
            summary = "로그아웃",
            description = "서버 세션을 무효화하고 Security Context를 초기화합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그아웃 성공")
    })
    ApiResponse<Void> logout(HttpServletRequest request, HttpServletResponse response);
}
