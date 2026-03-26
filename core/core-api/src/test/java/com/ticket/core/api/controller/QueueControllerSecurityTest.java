package com.ticket.core.api.controller;

import com.ticket.core.config.LoginMemberArgumentResolver;
import com.ticket.core.config.security.MemberPrincipal;
import com.ticket.core.domain.queue.command.exit.ExitQueueUseCase;
import com.ticket.core.domain.queue.command.join.JoinQueueUseCase;
import com.ticket.core.domain.queue.query.status.GetQueueStatusUseCase;
import com.ticket.core.domain.member.model.Role;
import com.ticket.core.support.ApiControllerAdvice;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("NonAsciiCharacters")
class QueueControllerSecurityTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        QueueController controller = new QueueController(
                Mockito.mock(JoinQueueUseCase.class),
                Mockito.mock(GetQueueStatusUseCase.class),
                Mockito.mock(ExitQueueUseCase.class)
        );
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new LoginMemberArgumentResolver())
                .setControllerAdvice(new ApiControllerAdvice())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void 인증되지_않은_사용자는_queue_진입_api를_호출할_수_없다() throws Exception {
        mockMvc.perform(post("/api/v1/queue/performances/10/enter")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void 인증된_사용자는_queue_상태조회_api를_호출할_수_있다() throws Exception {
        MemberPrincipal principal = new MemberPrincipal(100L, Role.MEMBER);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );

        mockMvc.perform(get("/api/v1/queue/performances/10/status")
                        .param("queueEntryId", "qe-10"))
                .andExpect(status().isOk());
    }
}
