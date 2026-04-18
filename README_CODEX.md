# Ticket Backend Codex Guide

이 문서는 이 저장소의 최신 코드 기준 개발자 온보딩 문서다.
기존 [README.md](c:/Users/mn040/IdeaProjects/ticket/README.md), [README_JUNIE.md](c:/Users/mn040/IdeaProjects/ticket/README_JUNIE.md)는 아이디어 초안과 오래된 요구사항이 섞여 있어 현재 코드와 차이가 있다. 실제 개발과 리뷰는 이 문서를 기준으로 보는 것을 권장한다.

## 1. 프로젝트 요약

이 프로젝트는 공연/전시 티켓팅 백엔드다. 현재 구현의 중심은 아래 5가지다.

- 인증/회원: 이메일 회원가입, 로그인, JWT 갱신, OAuth2 로그인 URL 조회 및 토큰 교환
- 공연/전시 조회: 쇼, 장르, 메타 코드, 회차/좌석 레이아웃 조회
- 좌석 선택/선점: Redis 기반 selection, hold, 만료 처리, 실시간 좌석 상태 반영
- 주문: `PENDING` 주문 생성, 조회, 취소, 만료 처리
- 대기열: 회차별 대기열 진입, 상태 조회, 이탈, 입장 토큰 발급

아직 없는 영역도 분명하다.

- 결제 승인/실패/콜백 처리: 아직 미구현
- 최종 좌석 판매 확정 플로우: 상태 모델은 일부 준비되어 있으나 실제 결제 유스케이스는 없음
  - 대기열은 기본 진입/조회/이탈 흐름과 토큰 발급까지 구현되어 있고, 정책 고도화가 남아 있다.

즉 현재 프로젝트는 "티켓팅 핵심 선점/주문 흐름과 기본 대기열까지 구현된 상태"이고, 결제와 대기열 정책 고도화가 다음 단계다.

## 2. 현재 구현 상태

### 2.1 인증

- `POST /api/v1/auth/signup`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`
- `GET /api/v1/auth/social/urls`
- `POST /api/v1/auth/oauth2/token`

구현 포인트:

- API 인증은 JWT 기반 stateless 방식이다.
- OAuth2 인가 흐름은 별도 filter chain에서 처리한다.
- Refresh token과 OAuth2 1회용 코드는 Redis를 사용한다.
- 공개 GET API를 제외한 대부분 API는 인증이 필요하다.

관련 코드:

- [AuthController.java](c:/Users/mn040/IdeaProjects/ticket/core/core-api/src/main/java/com/ticket/core/api/controller/AuthController.java)
- [SecurityConfig.java](c:/Users/mn040/IdeaProjects/ticket/core/core-api/src/main/java/com/ticket/core/config/security/SecurityConfig.java)

### 2.2 쇼/회차/좌석 조회

- 쇼 목록, 최신 쇼, 오픈 예정 쇼, 검색, 상세 조회
- 쇼 기준 좌석 정보/공연장 레이아웃 조회
- 회차별 좌석 상태 조회
- 회차별 등급별 잔여 좌석 수 조회

구현 포인트:

- 좌석 상태는 DB 상태와 Redis 점유 상태를 합쳐서 계산한다.
- 잔여 좌석 수 역시 Redis `SELECTING/HOLDING` 을 반영하도록 보강된 상태다.

관련 코드:

- [ShowController.java](c:/Users/mn040/IdeaProjects/ticket/core/core-api/src/main/java/com/ticket/core/api/controller/ShowController.java)
- [PerformanceController.java](c:/Users/mn040/IdeaProjects/ticket/core/core-api/src/main/java/com/ticket/core/api/controller/PerformanceController.java)
- [GetSeatStatusUseCase.java](c:/Users/mn040/IdeaProjects/ticket/core/core-domain/src/main/java/com/ticket/core/domain/performanceseat/query/GetSeatStatusUseCase.java)
- [GetSeatAvailabilityUseCase.java](c:/Users/mn040/IdeaProjects/ticket/core/core-domain/src/main/java/com/ticket/core/domain/performanceseat/query/GetSeatAvailabilityUseCase.java)

### 2.3 좌석 선택(selection)

- `POST /api/v1/performances/{performanceId}/seats/{seatId}/select`
- `DELETE /api/v1/performances/{performanceId}/seats/{seatId}/select`
- `DELETE /api/v1/performances/{performanceId}/seats/select`

현재 정책:

- selection은 UX 보조 상태다.
- hold의 필수 선행 조건이 아니다.
- 다른 사용자가 selection 중이어도 hold가 성공할 수 있다.
- selection은 Redis TTL로 자동 만료된다.
- 만료 시 Redis expired listener가 즉시 `DESELECTED` 이벤트를 전파한다.

관련 코드:

- [SeatSelectionController.java](c:/Users/mn040/IdeaProjects/ticket/core/core-api/src/main/java/com/ticket/core/api/controller/SeatSelectionController.java)
- [SeatSelectionService.java](c:/Users/mn040/IdeaProjects/ticket/core/core-domain/src/main/java/com/ticket/core/domain/performanceseat/command/SeatSelectionService.java)

### 2.4 좌석 선점(hold) + 주문 시작

- `POST /api/v1/performances/{performanceId}/holds`

현재 흐름:

1. 회차 유효성, 좌석 유효성, 수량 제한 검증
2. 같은 회원/같은 회차 `PENDING` 주문 여부 검증
3. Redis에 좌석 hold 생성
4. DB에 `PENDING` 주문 생성
5. hold history 기록
6. `201 Created` 와 `orderKey` 반환

현재 정책:

- 회차당 같은 회원은 `PENDING` 주문 1건만 허용
- hold는 다중 좌석 all-or-nothing으로 생성
- hold는 `holdKey` 로 식별
- hold 만료 시 주문 만료 흐름과 연결

관련 코드:

- [HoldController.java](c:/Users/mn040/IdeaProjects/ticket/core/core-api/src/main/java/com/ticket/core/api/controller/HoldController.java)
- [CreateOrderUseCase.java](c:/Users/mn040/IdeaProjects/ticket/core/core-domain/src/main/java/com/ticket/core/domain/order/command/create/CreateOrderUseCase.java)
- [HoldManager.java](c:/Users/mn040/IdeaProjects/ticket/core/core-domain/src/main/java/com/ticket/core/domain/hold/command/HoldManager.java)
- [HoldHistoryRecorder.java](c:/Users/mn040/IdeaProjects/ticket/core/core-domain/src/main/java/com/ticket/core/domain/hold/command/HoldHistoryRecorder.java)

### 2.5 주문 종료

- `GET /api/v1/orders/{orderKey}`
- `DELETE /api/v1/orders/{orderKey}`

현재 구현:

- 주문 상세 조회
- 사용자 취소
- 스케줄러 기반 만료
- Redis expired listener 기반 즉시 만료

현재 정책:

- 외부 식별자는 `orderKey`
- 주문 조회는 `GetOrderDetailUseCase`, 취소는 `CancelOrderUseCase` 로 분리되어 있다
- 주문 취소/만료 후 hold 해제와 좌석 상태 broadcast가 이어진다

관련 코드:

- [OrderController.java](c:/Users/mn040/IdeaProjects/ticket/core/core-api/src/main/java/com/ticket/core/api/controller/OrderController.java)
- [GetOrderDetailUseCase.java](c:/Users/mn040/IdeaProjects/ticket/core/core-domain/src/main/java/com/ticket/core/domain/order/query/GetOrderDetailUseCase.java)
- [CancelOrderUseCase.java](c:/Users/mn040/IdeaProjects/ticket/core/core-domain/src/main/java/com/ticket/core/domain/order/command/cancel/CancelOrderUseCase.java)
- [OrderExpirationScheduler.java](c:/Users/mn040/IdeaProjects/ticket/core/core-domain/src/main/java/com/ticket/core/domain/order/command/expire/OrderExpirationScheduler.java)
- [RedisKeyExpirationListener.java](c:/Users/mn040/IdeaProjects/ticket/core/core-infra/src/main/java/com/ticket/core/infra/redis/RedisKeyExpirationListener.java)

### 2.6 대기열(queue)

- `POST /api/v1/queue/performances/{performanceId}/enter`
- `GET /api/v1/queue/performances/{performanceId}/status`
- `POST /api/v1/queue/performances/{performanceId}/leave`

현재 구현:

- 회차별 대기열 진입
- 현재 대기 상태 또는 입장 토큰 상태 조회
- 대기 또는 입장 상태 종료
- 설정 기반 기본 정책(`app.queue`) 적용

현재 정책:

- 회차 단위로 대기열을 운영한다
- 즉시 입장 가능하면 토큰을 발급하고, 아니면 현재 순번을 반환한다
- 입장/이탈은 분산락으로 직렬화한다

관련 코드:

- [QueueController.java](c:/Users/mn040/IdeaProjects/ticket/core/core-api/src/main/java/com/ticket/core/api/controller/QueueController.java)
- [JoinQueueUseCase.java](c:/Users/mn040/IdeaProjects/ticket/core/core-domain/src/main/java/com/ticket/core/domain/queue/command/join/JoinQueueUseCase.java)
- [GetQueueStatusUseCase.java](c:/Users/mn040/IdeaProjects/ticket/core/core-domain/src/main/java/com/ticket/core/domain/queue/query/status/GetQueueStatusUseCase.java)
- [ExitQueueUseCase.java](c:/Users/mn040/IdeaProjects/ticket/core/core-domain/src/main/java/com/ticket/core/domain/queue/command/exit/ExitQueueUseCase.java)

## 3. 현재 핵심 도메인 모델

### 3.1 Order

- 내부 PK: `id`
- 외부 식별자: `orderKey`
- hold 연결 키: `holdKey`
- 주요 상태: `PENDING`, `CONFIRMED`, `EXPIRED`, `CANCELED`, `PAYMENT_FAILED`

주의:

- 상태 모델은 결제 성공/실패를 수용할 준비가 되어 있다.
- 하지만 실제 `confirm`, `failPayment` 를 호출하는 결제 유스케이스는 아직 없다.

### 3.2 Hold

- Redis hold key: 좌석 단위 key
- Redis hold meta key: `hold:key:{holdKey}`
- DB 이력: `HOLD_HISTORY`

hold는 현재 DB 엔티티가 아니라 Redis 기반 임시 점유 상태이며, DB에는 이력만 남긴다.

### 3.3 Selection

- Redis TTL 기반 임시 UX 상태
- 비즈니스 핵심 상태가 아니라 사용자 경험 보조 상태

## 4. 기술 스택

- Java 25 toolchain
- Spring Boot 4.0.2
- Spring Web
- Spring Security + JWT + OAuth2 Client
- Spring Data JPA
- Querydsl
- Redis + Redisson
- WebSocket(STOMP + SockJS)
- H2(local), Oracle(prod)
- Swagger / Springdoc

관련 설정:

- [build.gradle](c:/Users/mn040/IdeaProjects/ticket/build.gradle)
- [core-api/build.gradle](c:/Users/mn040/IdeaProjects/ticket/core/core-api/build.gradle)
- [settings.gradle](c:/Users/mn040/IdeaProjects/ticket/settings.gradle)

## 5. 모듈 구조

### 5.1 Gradle 멀티 모듈

- `core:core-api`
  - 실제 Spring Boot API 애플리케이션
- `core:core-domain`
  - 비즈니스 규칙, 유스케이스, 저장소, 도메인 포트를 포함한 business core 모듈
- `core:core-infra`
  - `core-domain` 포트를 구현하는 기술 어댑터 모듈
- `storage:redis-core`
  - Redis 관련 공통 설정/리소스
- `support:logging`
  - 로깅 관련 공통 리소스

### 5.2 core-api 패키지 흐름

- `api/controller`
  - HTTP 진입점
- `config`
  - 보안, WebSocket, Redis expiration listener, 스케줄링 등
- `support`
  - 공통 응답, 인증 보조 유틸리티

### 5.3 core-domain 패키지 흐름

- `domain/*/command`
  - 상태 변경 유스케이스
- `domain/*/query`
  - 조회 유스케이스와 조회 전용 모델
- `domain/*/model`, `repository`, `store`
  - 도메인 모델과 영속/임시 저장소
- `domain/*/infra`
  - 기능별 기술 구현체
- `infra`
  - Querydsl, Redis listener, 분산락 등 공통 인프라

## 6. 실시간 처리 구조

### 6.1 WebSocket

- endpoint: `/ws`
- broker prefix: `/topic`

현재 용도:

- 좌석 선택/해제
- hold/release 이후 좌석 상태 변경 알림

관련 코드:

- [WebSocketConfig.java](c:/Users/mn040/IdeaProjects/ticket/core/core-api/src/main/java/com/ticket/core/config/WebSocketConfig.java)
- [SeatEventPublisher.java](c:/Users/mn040/IdeaProjects/ticket/core/core-domain/src/main/java/com/ticket/core/domain/performanceseat/infra/realtime/SeatEventPublisher.java)

### 6.2 Redis expiration listener

현재 구현:

- selection 만료 즉시 `DESELECTED` 전파
- hold meta 만료 즉시 주문 만료 처리 트리거

관련 코드:

- [RedisExpirationListenerConfig.java](c:/Users/mn040/IdeaProjects/ticket/core/core-infra/src/main/java/com/ticket/core/infra/redis/RedisExpirationListenerConfig.java)
- [RedisKeyExpirationListener.java](c:/Users/mn040/IdeaProjects/ticket/core/core-infra/src/main/java/com/ticket/core/infra/redis/RedisKeyExpirationListener.java)

## 7. 동시성 전략

현재 구현에서 중요한 락은 두 종류다.

- 회원/회차 단위 락
  - 같은 회원이 같은 회차에 `PENDING` 주문을 여러 개 시작하는 것 방지
- 좌석 단위 멀티락
  - 여러 좌석 hold를 all-or-nothing으로 보호

구현 방식:

- 커스텀 `@DistributedLock`
- `DistributedLockAop` 에서 Redisson lock / multi-lock 처리

관련 코드:

- [DistributedLock.java](c:/Users/mn040/IdeaProjects/ticket/core/core-domain/src/main/java/com/ticket/core/infra/lock/DistributedLock.java)
- [DistributedLockAop.java](c:/Users/mn040/IdeaProjects/ticket/core/core-infra/src/main/java/com/ticket/core/infra/lock/DistributedLockAop.java)

## 8. 로컬 실행

### 8.1 기본 환경

- JDK 25
- Redis 7

Redis는 루트의 [docker-compose.yml](c:/Users/mn040/IdeaProjects/ticket/docker-compose.yml) 로 띄울 수 있다.

```bash
docker compose up -d redis
```

### 8.2 애플리케이션 실행

로컬 프로파일은 H2 file DB + Redis 조합이다.

- DB: H2 file
- JPA DDL: `create`
- seed data: enabled

관련 설정:

- [application.yml](c:/Users/mn040/IdeaProjects/ticket/core/core-api/src/main/resources/application.yml)
- [application-local.yml](c:/Users/mn040/IdeaProjects/ticket/core/core-api/src/main/resources/application-local.yml)

예시 실행:

```bash
./gradlew :core:core-api:bootRun
```

컴파일 확인:

```bash
./gradlew :core:core-api:compileJava
```

Swagger:

- `/swagger-ui.html`
- `/api-docs`

## 9. 운영 반영 시 주의점

- prod는 Oracle 드라이버를 사용한다.
- `application-prod.yml` 에서 `ddl-auto: none` 이다.
- 따라서 스키마 변경이 생기면 DDL을 별도 반영해야 한다.
- `holdKey` 명명 변경처럼 DB 컬럼명이 바뀌는 경우는 특히 수동 마이그레이션 계획이 필요하다.

관련 설정:

- [application-prod.yml](c:/Users/mn040/IdeaProjects/ticket/core/core-api/src/main/resources/application-prod.yml)

## 10. 아직 없는 것

### 10.1 결제

현재 상태:

- 주문 상태 모델에는 결제 성공/실패를 수용할 필드가 있다.
- 하지만 실제 payment aggregate, controller, callback, PG 연동은 아직 없다.

예정 범위:

- 결제 시작 API
- PG callback 처리
- 결제 성공 시 `Order.confirm`
- 결제 실패 시 `Order.failPayment`
- 최종 좌석 판매 확정

### 10.2 대기열

현재 상태:

- queue 도메인과 API가 구현되어 있다.
- 회차별 진입/상태 조회/이탈과 입장 토큰 발급까지 동작한다.

예정 범위:

- 레벨별 정책 고도화
- 공연/회차별 운영 정책 분리
- 입장 허용 윈도우와 운영 관제 기준 정교화

## 11. 개선이 필요한 지점

현재 코드 기준으로 우선순위가 높은 항목들이다.

- 결제 도메인 추가와 `CONFIRMED` 전이 완성
- 대기열 정책과 공연 단위 트래픽 제어 기준 고도화
- Redis key scan 기반 조회 구조 최적화
- hold/order 관련 DB 마이그레이션 정리
- 인코딩 깨진 기존 문서 및 일부 주석 정리
- 운영용 마이그레이션 도구(Flyway 또는 Liquibase) 도입 검토

## 12. 추천 문서

- 제품/기능 요약: [README_PRODUCT.md](c:/Users/mn040/IdeaProjects/ticket/README_PRODUCT.md)
- 구조/아키텍처 요약: [README_ARCHITECTURE.md](c:/Users/mn040/IdeaProjects/ticket/README_ARCHITECTURE.md)

