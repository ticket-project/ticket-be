# Ticket Backend Architecture

## 1. 아키텍처 개요

현재 애플리케이션은 멀티 모듈 Spring Boot 구조다.

- `core:core-api`
- `core:core-enum`
- `storage:redis-core`
- `support:logging`

실행 애플리케이션은 `core:core-api` 하나이며, 나머지는 보조 모듈이다.

핵심 구조는 아래 3개 축으로 이해하면 된다.

- HTTP/JWT/OAuth2/WebSocket 진입 계층
- 도메인별 use case / application / domain service
- RDB + Redis 조합 상태 저장

## 2. 계층 구조

### 2.1 Controller

역할:

- 요청/응답 매핑
- 인증 principal 주입
- use case 1개 호출

예시:

- [AuthController.java](c:/Users/mn040/IdeaProjects/ticket/core/core-api/src/main/java/com/ticket/core/api/controller/AuthController.java)
- [HoldController.java](c:/Users/mn040/IdeaProjects/ticket/core/core-api/src/main/java/com/ticket/core/api/controller/HoldController.java)
- [OrderController.java](c:/Users/mn040/IdeaProjects/ticket/core/core-api/src/main/java/com/ticket/core/api/controller/OrderController.java)

### 2.2 UseCase

역할:

- 요청 단위 흐름 orchestration
- 관련 컴포넌트 조합
- 트랜잭션 경계의 중심

예시:

- [StartOrderUseCase.java](c:/Users/mn040/IdeaProjects/ticket/core/core-api/src/main/java/com/ticket/core/domain/order/command/usecase/StartOrderUseCase.java)
- [TerminateOrderUseCase.java](c:/Users/mn040/IdeaProjects/ticket/core/core-api/src/main/java/com/ticket/core/domain/order/command/usecase/TerminateOrderUseCase.java)
- [GetOrderDetailUseCase.java](c:/Users/mn040/IdeaProjects/ticket/core/core-api/src/main/java/com/ticket/core/domain/order/query/usecase/GetOrderDetailUseCase.java)

### 2.3 Application / DomainService / Manager

이 프로젝트는 역할 기반 응집을 택하고 있다.

- `CreateOrderApplicationService`
  - order, orderSeat 생성
- `HoldHistoryRecorder`
  - hold history 기록
- `HoldManager`
  - Redis hold 생성/해제
- `OrderLifecycleDomainService`
  - 주문/좌석/hold history 상태 전이 규칙

즉, 비즈니스 흐름은 use case가 읽고, 저장/상태/인프라 역할은 하위 컴포넌트로 나뉜 구조다.

## 3. 상태 저장 전략

## 3.1 RDB

RDB는 영속 상태와 조회 기준 데이터에 사용한다.

대표 엔티티:

- `Order`
- `OrderSeat`
- `HoldHistory`
- `PerformanceSeat`
- `Performance`
- `Show`
- `Member`

역할:

- 주문 영속화
- 좌석/회차/쇼 조회
- hold 이력 저장
- 사용자 정보 저장

## 3.2 Redis

Redis는 짧은 TTL을 가지는 실시간 상태와 인증 보조 상태에 사용한다.

대표 사용처:

- 좌석 selection
- 좌석 hold
- hold meta
- refresh token
- OAuth2 1회용 코드

역할:

- 빠른 점유 상태 반영
- TTL 기반 자동 만료
- 실시간 경쟁 구간 처리

## 4. 실시간 처리 구조

### 4.1 Selection

- 저장: Redis
- 만료: TTL
- 후처리: Redis expired listener
- 전파: WebSocket `DESELECTED`

### 4.2 Hold

- 저장: Redis
- 식별: `holdKey`
- 부가 저장: hold meta JSON
- 만료:
  - Redis expired listener 즉시 반응
  - 스케줄러 보조 정리

### 4.3 Order

- 저장: RDB
- 식별:
  - 내부 `id`
  - 외부 `orderKey`
  - hold 연결 `holdKey`

## 5. 주문 시작/종료 흐름

### 5.1 주문 시작

1. `HoldController`
2. `StartOrderUseCase`
3. 좌석/회차 검증
4. 회원/회차 단위 분산 락
5. `HoldManager.createHold`
6. `CreateOrderApplicationService.createPendingOrder`
7. `HoldHistoryRecorder.recordActiveHold`
8. `orderKey` 반환

### 5.2 주문 종료

1. 사용자 취소 또는 만료 감지
2. `TerminateOrderUseCase`
3. `OrderLifecycleDomainService`
4. after-commit 이벤트
5. `HoldManager.release`
6. 좌석 상태 broadcast

## 6. 동시성 제어

현재는 Redis/Redisson 기반 분산 락을 사용한다.

### 6.1 주문 시작 락

- 범위: `memberId + performanceId`
- 목적: 같은 회원/같은 회차 `PENDING` 중복 생성 방지

### 6.2 좌석 hold 멀티락

- 범위: `performanceId + seatIds`
- 목적: 같은 좌석 동시 hold 방지

### 6.3 구현 방식

- 커스텀 애노테이션 `@DistributedLock`
- `DistributedLockAop` 에서 Redisson multi-lock 지원

## 7. 만료 처리

현재 만료는 2중 안전장치 구조다.

### 7.1 Redis expired listener

- selection 만료 즉시 처리
- hold meta 만료 즉시 주문 만료 트리거

### 7.2 스케줄러

- 주기적으로 만료된 `PENDING` 주문 정리
- listener 누락이나 운영 이슈를 보조

즉, listener가 빠른 반응을 담당하고 스케줄러가 보정 역할을 한다.

## 8. 보안 구조

### 8.1 API 보안

- JWT stateless 인증
- 공개 GET API만 permit
- 나머지는 authenticated

### 8.2 OAuth2 보안

- 별도 filter chain
- 세션 사용 허용
- callback 성공 후 1회용 code를 Redis에 저장하고 교환

### 8.3 WebSocket 보안

- STOMP CONNECT 시 JWT 인터셉터 적용

## 9. 현재 아키텍처의 강점

- selection / hold / order 역할 분리가 비교적 명확하다
- Redis TTL + listener + scheduler 로 만료 복원력이 있다
- order 외부 식별자를 `orderKey` 로 분리했다
- hold 이력 기록 책임도 hold 쪽으로 이동했다

## 10. 현재 아키텍처의 한계

### 10.1 결제 미구현

- 상태 모델은 준비됐지만 결제 aggregate가 없다
- `CONFIRMED` 까지 닫히지 않았다

### 10.2 대기열 미구현

- 인기 공연 진입 제어 계층이 아직 없다

### 10.3 Redis 조회 비용

- 일부 조회는 key pattern scan 기반이다
- 트래픽 증가 시 구조 개선이 필요하다

### 10.4 DB 마이그레이션 체계 부족

- prod는 `ddl-auto: none`
- 스키마 변경 시 마이그레이션 절차가 문서화/자동화되어 있지 않다

## 11. 권장 다음 단계

1. Payment aggregate와 payment controller 추가
2. 결제 성공 시 order confirm / performanceSeat reserve 연결
3. Queue 도메인 추가
4. Flyway 또는 Liquibase 도입
5. Redis key scan 구조 최적화

