package com.ticket.core.config;

import com.ticket.core.domain.auth.oauth2.KakaoApiClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.service.registry.ImportHttpServices;

/**
 * 외부 HTTP 서비스 클라이언트 등록
 * Spring Framework 7 / Spring Boot 4의 @ImportHttpServices 활용
 *
 * base-url, timeout 등은 application.yml의
 * spring.http.serviceclient.{group} 프로퍼티로 자동 설정됨
 * (spring-boot-starter-restclient 의존성 필수)
 */
@Configuration
@ImportHttpServices(group = "kakao", types = KakaoApiClient.class)
public class HttpServiceConfig {
}
