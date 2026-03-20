package com.ticket.core.api.controller;

import com.ticket.core.config.LoginMemberArgumentResolver;
import com.ticket.core.domain.member.MemberPrincipal;
import com.ticket.core.domain.queue.model.QueueEntryStatus;
import com.ticket.core.domain.queue.usecase.ExitQueueUseCase;
import com.ticket.core.domain.queue.usecase.GetQueueStatusUseCase;
import com.ticket.core.domain.queue.usecase.JoinQueueUseCase;
import com.ticket.core.domain.queue.usecase.QueueEntryId;
import com.ticket.core.enums.Role;
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

import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("NonAsciiCharacters")
class QueueControllerContractTest {

    private static final MemberPrincipal MEMBER = new MemberPrincipal(100L, Role.MEMBER);

    private final JoinQueueUseCase joinQueueUseCase = Mockito.mock(JoinQueueUseCase.class);
    private final GetQueueStatusUseCase getQueueStatusUseCase = Mockito.mock(GetQueueStatusUseCase.class);
    private final ExitQueueUseCase exitQueueUseCase = Mockito.mock(ExitQueueUseCase.class);

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        QueueController controller = new QueueController(joinQueueUseCase, getQueueStatusUseCase, exitQueueUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new LoginMemberArgumentResolver())
                .setControllerAdvice(new ApiControllerAdvice())
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(MEMBER, null, MEMBER.getAuthorities())
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void 대기열_진입_API는_200과_응답_계약을_유지한다() throws Exception {
        when(joinQueueUseCase.execute(new JoinQueueUseCase.Input(10L, 100L)))
                .thenReturn(new JoinQueueUseCase.Output(QueueEntryStatus.WAITING, "qe-1", 3L, null, null));

        mockMvc.perform(post("/api/v1/queue/performances/10/enter")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.status").value("WAITING"))
                .andExpect(jsonPath("$.data.queueEntryId").value("qe-1"))
                .andExpect(jsonPath("$.data.position").value(3))
                .andExpect(jsonPath("$.error").isEmpty());
    }

    @Test
    void 대기열_상태조회_API는_200과_응답_계약을_유지한다() throws Exception {
        when(getQueueStatusUseCase.execute(new GetQueueStatusUseCase.Input(10L, 100L, QueueEntryId.from("qe-1"))))
                .thenReturn(new GetQueueStatusUseCase.Output(
                        QueueEntryStatus.ADMITTED,
                        "qe-1",
                        null,
                        "qt-1",
                        LocalDateTime.of(2026, 3, 20, 18, 0)
                ));

        mockMvc.perform(get("/api/v1/queue/performances/10/status")
                        .param("queueEntryId", "qe-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data.status").value("ADMITTED"))
                .andExpect(jsonPath("$.data.queueEntryId").value("qe-1"))
                .andExpect(jsonPath("$.data.queueToken").value("qt-1"))
                .andExpect(jsonPath("$.error").isEmpty());
    }

    @Test
    void 대기열_이탈_API는_200과_성공_응답_계약을_유지한다() throws Exception {
        mockMvc.perform(post("/api/v1/queue/performances/10/leave")
                        .param("queueEntryId", "qe-1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.error").isEmpty());
    }
}
