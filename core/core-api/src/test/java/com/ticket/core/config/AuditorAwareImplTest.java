package com.ticket.core.config;

import com.ticket.core.config.security.MemberPrincipal;
import com.ticket.core.domain.member.model.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
class AuditorAwareImplTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void 인증된_MemberPrincipal이_있으면_memberId를_감사자로_반환한다() {
        MemberPrincipal principal = new MemberPrincipal(7L, Role.MEMBER);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        AuditorAwareImpl auditorAware = new AuditorAwareImpl();

        assertThat(auditorAware.getCurrentAuditor()).contains("7");
    }

    @Test
    void 인증정보가_없으면_빈값을_반환한다() {
        AuditorAwareImpl auditorAware = new AuditorAwareImpl();

        assertThat(auditorAware.getCurrentAuditor()).isEmpty();
    }
}
