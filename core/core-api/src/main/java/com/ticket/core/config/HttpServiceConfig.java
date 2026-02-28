package com.ticket.core.config;

import com.ticket.core.domain.auth.oauth2.KakaoApiClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.service.registry.ImportHttpServices;

/**
 * 외부 HTTP 서비스 클라이언트 등록
 * Spring Framework 7 / Spring Boot 4의 @ImportHttpServices 활용
 */
@Configuration
@ImportHttpServices(types = KakaoApiClient.class)
public class HttpServiceConfig {
}
