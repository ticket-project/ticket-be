package com.ticket.core.api.controller;

import com.ticket.core.api.controller.request.AddMemberRequest;
import com.ticket.core.api.controller.request.LoginMemberRequest;
import com.ticket.core.domain.auth.AuthService;
import com.ticket.core.domain.auth.SessionConst;
import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.MemberDetails;
import com.ticket.core.support.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "인증", description = "회원가입 및 로그인 API")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(final AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "회원가입", description = "새로운 회원을 등록합니다")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "회원가입 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 (유효성 검증 실패)")
    })
    @PostMapping("/sign-up")
    public ApiResponse<Long> register(@RequestBody @Valid AddMemberRequest request) {
        return ApiResponse.success(authService.register(request.toAddMember()));
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/login")
    public ApiResponse<Long> login(@RequestBody @Valid LoginMemberRequest request, HttpServletRequest httpRequest) {
        final Member loginedMember = authService.login(request.getEmail(), request.getPassword());

        final HttpSession oldSession = httpRequest.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }
        final HttpSession session = httpRequest.getSession();
        session.setAttribute(SessionConst.LOGIN_MEMBER, new MemberDetails(loginedMember.getId(), loginedMember.getRole()));
        return ApiResponse.success(loginedMember.getId());
    }
}
