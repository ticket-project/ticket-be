package com.ticket.core.config;

import com.ticket.core.domain.member.MemberDetails;
import com.ticket.core.enums.Role;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {
    
    @Override
    public boolean supportsParameter(final MethodParameter parameter) {
        return MemberDetails.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public MemberDetails resolveArgument(
            final MethodParameter parameter,
            final ModelAndViewContainer mavContainer,
            final NativeWebRequest webRequest,
            final WebDataBinderFactory binderFactory
    ) throws Exception {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        // JWT 필터에서 설정한 memberId를 가져옴
        final Long memberId = (Long) authentication.getPrincipal();
        
        // 권한에서 Role 추출
        final String roleAuthority = authentication.getAuthorities().stream()
                .findFirst()
                .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                .orElse("MEMBER");

        return new MemberDetails(memberId, Role.valueOf(roleAuthority));
    }
}
