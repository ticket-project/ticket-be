package com.ticket.core.api.controller;

import com.ticket.core.api.controller.request.AddMemberRequest;
import com.ticket.core.api.controller.request.LoginMemberRequest;
import com.ticket.core.domain.auth.AuthService;
import com.ticket.core.domain.auth.SessionConst;
import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.MemberDetails;
import com.ticket.core.support.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(final AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/v0")
    public ApiResponse<Long> register(@RequestBody @Valid AddMemberRequest request) {
        return ApiResponse.success(authService.register(request.toAddMember()));
    }

    @PostMapping("/v0/login")
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
