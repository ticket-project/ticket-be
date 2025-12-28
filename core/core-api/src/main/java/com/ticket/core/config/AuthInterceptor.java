package com.ticket.core.config;

import com.ticket.core.domain.auth.SessionConst;
import com.ticket.core.domain.member.MemberDetails;
import com.ticket.core.enums.Role;
import com.ticket.core.support.exception.AuthException;
import com.ticket.core.support.exception.ErrorType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) throws Exception {
        final HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(SessionConst.LOGIN_MEMBER) == null) {
            throw new AuthException(ErrorType.AUTHENTICATION_ERROR);
        }

        final MemberDetails memberDetails = (MemberDetails) session.getAttribute(SessionConst.LOGIN_MEMBER);
        final String uri = request.getRequestURI();
        if (uri.startsWith("/api/admin")) {
            if (memberDetails.getRole() != Role.ADMIN) {
                throw new AuthException(ErrorType.AUTHORIZATION_ERROR);
            }
        }

        return true;
    }
}
