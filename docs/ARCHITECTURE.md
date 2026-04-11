# Architecture

## 모듈 의존 방향

```
core-api  ──depends──▶  core-domain  ──depends──▶  redis-core
   │                        │
   └── support:logging      └── storage:redis-core
```

- `core-api`는 `core-domain`에 의존한다 (역방향 금지)
- `core-domain`은 `core-api`의 존재를 모른다
- `redis-core`는 순수 연결 설정만 제공한다

## core-api 소유물

core-api에만 존재해야 하는 것:

| 분류 | 예시 |
|---|---|
| Response DTO | `AuthLoginResponse`, `OrderDetailResponse`, `ShowResponse` 등 전체 Response 클래스 |
| JWT/OAuth2 설정 | `JwtTokenService`, `JwtProperties`, `OAuth2EndpointConstants` |
| HTTP 유틸리티 | `CookieUtils` |
| Swagger 의존성 | `springdoc-openapi` |
| 컨트롤러 | 모든 `@RestController`, `@Controller` |
| WebSocket 설정 | `WebSocketConfig`, 핸들러 |

## core-domain 계층 구조

각 비즈니스 도메인 패키지(`order`, `hold`, `queue` 등) 내부 구조:

```
domain/{도메인명}/
├── model/          ← 엔티티, 값 객체, 열거형 (의존 없음)
├── command/        ← 쓰기 유스케이스, 서비스
├── query/          ← 읽기 유스케이스, 조회 서비스
├── event/          ← 도메인 이벤트, 이벤트 리스너
├── infra/          ← 외부 시스템 연동 (Redis, 메시징, HTTP 클라이언트)
└── store/          ← 저장소 구현 (JPA Repository, Redis Store)
```

## 인프라 격리 규칙 (ArchUnit 강제)

`..infra..` 패키지 외부에서 직접 참조 금지:

| 라이브러리 | 패키지 |
|---|---|
| Redisson | `org.redisson..` |
| Spring Data Redis | `org.springframework.data.redis..` |
| Spring Messaging | `org.springframework.messaging..` |
| HTTP Interface Client | `org.springframework.web.service.annotation..` (config 예외) |

위반 시 `CoreDomainArchitectureTest`에서 빌드 실패.

## 비즈니스 도메인 의존 관계

```
show ◀── performance ◀── performanceseat ◀── seat
                │
                ├── queue (독립)
                │
                └── order ──▶ hold
                               │
                               └── performanceseat (Redis 홀드/선택)

member ◀── auth
member ◀── order
member ◀── queue
```

## 기술 스택

| 레이어 | 기술 |
|---|---|
| 언어 | Java 25 |
| 프레임워크 | Spring Boot 4.0.2 |
| 빌드 | Gradle 8.x |
| DB | Oracle 21.3.0 (prod) / H2 (test) |
| 캐시/저장 | Redis 7.4 + Redisson 4.2.0 |
| API 문서 | SpringDoc OpenAPI 3.0.1 |
| 모니터링 | Actuator + Prometheus + Datadog |
| 쿼리 | QueryDSL 5.1.0 |
| 테스트 | JUnit 5, ArchUnit 1.4.1, Fixture Monkey 1.1.15 |
