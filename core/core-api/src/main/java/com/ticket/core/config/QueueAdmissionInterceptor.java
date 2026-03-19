package com.ticket.core.config;

import com.ticket.core.domain.queue.runtime.QueueTicketStore;
import com.ticket.core.domain.queue.support.QueuePolicyResolver;
import com.ticket.core.domain.queue.support.ResolvedQueuePolicy;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class QueueAdmissionInterceptor implements HandlerInterceptor {

    private final QueuePolicyResolver queuePolicyResolver;
    private final QueueTicketStore queueTicketStore;

    @Override
    @SuppressWarnings("unchecked")
    public boolean preHandle(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final Object handler
    ) {
        if (!requiresQueueAdmission(handler)) {
            return true;
        }

        final Long performanceId = extractPerformanceId(request);
        if (performanceId == null) {
            return true;
        }

        final ResolvedQueuePolicy policy = queuePolicyResolver.resolve(performanceId);
        if (!policy.enabled()) {
            return true;
        }

        final String queueToken = request.getHeader("X-Queue-Token");
        if (queueToken == null || queueToken.isBlank()) {
            throw new CoreException(ErrorType.QUEUE_TOKEN_REQUIRED);
        }
        if (!queueTicketStore.isValidToken(performanceId, queueToken)) {
            throw new CoreException(ErrorType.QUEUE_TOKEN_INVALID);
        }
        return true;
    }

    private boolean requiresQueueAdmission(final Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return false;
        }
        return AnnotatedElementUtils.hasAnnotation(handlerMethod.getMethod(), RequireQueueAdmission.class)
                || AnnotatedElementUtils.hasAnnotation(handlerMethod.getBeanType(), RequireQueueAdmission.class);
    }

    @SuppressWarnings("unchecked")
    private Long extractPerformanceId(final HttpServletRequest request) {
        final Map<String, String> pathVariables =
                (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (pathVariables != null && pathVariables.containsKey("performanceId")) {
            return Long.valueOf(pathVariables.get("performanceId"));
        }

        final String requestUri = request.getRequestURI();
        final String[] segments = requestUri.split("/");
        for (int i = 0; i < segments.length - 1; i++) {
            if ("performances".equals(segments[i])) {
                return Long.valueOf(segments[i + 1]);
            }
        }
        return null;
    }
}
