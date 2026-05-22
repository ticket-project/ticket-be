package com.ticket.core.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("NonAsciiCharacters")
class WebConfigTest {

    @Test
    void 대기열_입장_인터셉터를_공연_API_경로에_등록한다() {
        final QueueAdmissionInterceptor interceptor = mock(QueueAdmissionInterceptor.class);
        final InterceptorRegistry registry = mock(InterceptorRegistry.class);
        final InterceptorRegistration registration = mock(InterceptorRegistration.class);
        when(registry.addInterceptor(interceptor)).thenReturn(registration);

        new WebConfig(interceptor).addInterceptors(registry);

        verify(registry).addInterceptor(interceptor);
        verify(registration).addPathPatterns("/api/v1/performances/*/**");
    }
}
