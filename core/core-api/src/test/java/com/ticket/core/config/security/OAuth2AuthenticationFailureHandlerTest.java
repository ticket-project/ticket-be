package com.ticket.core.config.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

import static org.assertj.core.api.Assertions.assertThat;

class OAuth2AuthenticationFailureHandlerTest {

    @Test
    void shouldEncodeErrorMessageBeforeBuildingRedirectUrl() throws Exception {
        final OAuth2AuthenticationFailureHandler handler =
                new OAuth2AuthenticationFailureHandler("http://localhost:3000/login");
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final OAuth2AuthenticationException exception =
                new OAuth2AuthenticationException(
                        new OAuth2Error("authorization_request_not_found"),
                        "[authorization_request_not_found] "
                );

        handler.onAuthenticationFailure(request, response, exception);

        assertThat(response.getRedirectedUrl())
                .isEqualTo("http://localhost:3000/login?error=%5Bauthorization_request_not_found%5D");
    }
}
