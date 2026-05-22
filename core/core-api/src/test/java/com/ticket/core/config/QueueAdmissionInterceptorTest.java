package com.ticket.core.config;

import com.ticket.core.domain.queue.runtime.QueueTicketStore;
import com.ticket.core.domain.queue.support.QueuePolicy;
import com.ticket.core.domain.queue.support.QueuePolicyResolver;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@SuppressWarnings("NonAsciiCharacters")
class QueueAdmissionInterceptorTest {

    private final QueuePolicyResolver queuePolicyResolver = mock(QueuePolicyResolver.class);
    private final QueueTicketStore queueTicketStore = mock(QueueTicketStore.class);

    private QueueAdmissionInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new QueueAdmissionInterceptor(queuePolicyResolver, queueTicketStore);
    }

    @Test
    void 보호대상_API는_대기열_토큰이_없으면_거부한다() throws Exception {
        when(queuePolicyResolver.resolve(10L)).thenReturn(enabledPolicy());

        assertThatThrownBy(() -> interceptor.preHandle(
                request(null),
                new MockHttpServletResponse(),
                handler("protectedEndpoint")
        ))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> assertThat(((CoreException) exception).getErrorType())
                        .isEqualTo(ErrorType.QUEUE_TOKEN_REQUIRED));
    }

    @Test
    void 보호대상_API는_대기열_토큰이_유효하지_않으면_거부한다() throws Exception {
        when(queuePolicyResolver.resolve(10L)).thenReturn(enabledPolicy());
        when(queueTicketStore.isValidToken(10L, "invalid-token")).thenReturn(false);

        assertThatThrownBy(() -> interceptor.preHandle(
                request("invalid-token"),
                new MockHttpServletResponse(),
                handler("protectedEndpoint")
        ))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> assertThat(((CoreException) exception).getErrorType())
                        .isEqualTo(ErrorType.QUEUE_TOKEN_INVALID));
    }

    @Test
    void 보호대상_API라도_대기열_정책이_비활성화되어_있으면_통과한다() throws Exception {
        when(queuePolicyResolver.resolve(10L)).thenReturn(disabledPolicy());

        final boolean result = interceptor.preHandle(
                request(null),
                new MockHttpServletResponse(),
                handler("protectedEndpoint")
        );

        assertThat(result).isTrue();
        verifyNoInteractions(queueTicketStore);
    }

    @Test
    void 어노테이션이_없는_API는_대기열_검사를_하지_않는다() throws Exception {
        final boolean result = interceptor.preHandle(
                request(null),
                new MockHttpServletResponse(),
                handler("openEndpoint")
        );

        assertThat(result).isTrue();
        verifyNoInteractions(queuePolicyResolver, queueTicketStore);
    }

    private MockHttpServletRequest request(final String queueToken) {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, Map.of("performanceId", "10"));
        if (queueToken != null) {
            request.addHeader("X-Queue-Token", queueToken);
        }
        return request;
    }

    private HandlerMethod handler(final String methodName) throws NoSuchMethodException {
        return new HandlerMethod(new TestController(), TestController.class.getDeclaredMethod(methodName));
    }

    private QueuePolicy enabledPolicy() {
        return new QueuePolicy(true, null, 1, Duration.ofMinutes(10), Duration.ofHours(1));
    }

    private QueuePolicy disabledPolicy() {
        return new QueuePolicy(false, null, 1, Duration.ofMinutes(10), Duration.ofHours(1));
    }

    private static class TestController {

        @RequireQueueAdmission
        void protectedEndpoint() {
        }

        void openEndpoint() {
        }
    }
}
