package com.ticket.core.domain.queue.support;

import com.ticket.core.domain.queue.runtime.QueueRuntimeStore;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
class QueueTokenGatekeeperTest {

    @Mock
    private QueueRuntimeStore queueRuntimeStore;

    @InjectMocks
    private QueueTokenGatekeeper queueTokenGatekeeper;

    @Test
    void 토큰이_null이면_QUEUE_TOKEN_REQUIRED_예외를_던진다() {
        assertThatThrownBy(() -> queueTokenGatekeeper.assertAccessible(10L, null))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> assertThat(((CoreException) exception).getErrorType()).isEqualTo(ErrorType.QUEUE_TOKEN_REQUIRED));
    }

    @Test
    void 토큰이_blank이면_QUEUE_TOKEN_REQUIRED_예외를_던진다() {
        assertThatThrownBy(() -> queueTokenGatekeeper.assertAccessible(10L, " "))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> assertThat(((CoreException) exception).getErrorType()).isEqualTo(ErrorType.QUEUE_TOKEN_REQUIRED));
    }

    @Test
    void 유효하지_않은_토큰이면_QUEUE_TOKEN_INVALID_예외를_던진다() {
        when(queueRuntimeStore.isValidToken(10L, "qt-invalid")).thenReturn(false);

        assertThatThrownBy(() -> queueTokenGatekeeper.assertAccessible(10L, "qt-invalid"))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> assertThat(((CoreException) exception).getErrorType()).isEqualTo(ErrorType.QUEUE_TOKEN_INVALID));
    }

    @Test
    void 유효한_토큰이면_통과한다() {
        when(queueRuntimeStore.isValidToken(10L, "qt-valid")).thenReturn(true);

        queueTokenGatekeeper.assertAccessible(10L, "qt-valid");
    }
}
