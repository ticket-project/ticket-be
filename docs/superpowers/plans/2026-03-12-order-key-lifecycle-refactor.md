# Order Key Lifecycle Refactor Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 주문 식별자를 `orderKey` 로 전환하고 hold/order 만료·해제 흐름을 단일 주문 생명주기 서비스로 재구성한다.

**Architecture:** `Order` 를 외부 식별자와 상태 전이의 중심으로 재구성한다. 주문 시작은 hold 생성과 주문 생성을 하나의 흐름으로 묶고, 취소/만료/결제 실패/확정은 단일 lifecycle 서비스만 상태를 변경하게 한다. Redis TTL listener 와 스케줄러는 만료 감지만 맡고 실제 처리 로직은 공통 서비스로 수렴한다.

**Tech Stack:** Java, Spring Boot, Spring Data JPA, Redis/Redisson

---

## Chunk 1: 식별자 전환

### Task 1: Order 엔티티와 저장소에 orderKey 도입

**Files:**
- Modify: `core/core-api/src/main/java/com/ticket/core/domain/order/model/Order.java`
- Modify: `core/core-api/src/main/java/com/ticket/core/domain/order/repository/OrderRepository.java`
- Modify: `core/core-api/src/main/java/com/ticket/core/domain/order/finder/OrderFinder.java`

- [ ] `Order` 에 unique `orderKey` 필드를 추가하고 생성자에서 발급한다.
- [ ] `OrderRepository` 에 `findByOrderKey`, `findByOrderKeyAndMemberId` 조회를 추가한다.
- [ ] `OrderFinder` 를 `orderKey` 기준 조회 메서드로 전환하고 `id` 기반 외부 조회 메서드를 제거한다.
- [ ] 컴파일 확인: `./gradlew :core:core-api:compileJava`
- [ ] 커밋

### Task 2: 외부 API와 응답을 orderKey 기준으로 전환

**Files:**
- Modify: `core/core-api/src/main/java/com/ticket/core/api/controller/HoldController.java`
- Modify: `core/core-api/src/main/java/com/ticket/core/api/controller/OrderController.java`
- Modify: `core/core-api/src/main/java/com/ticket/core/api/controller/response/OrderDetailResponse.java`
- Modify: `core/core-api/src/main/java/com/ticket/core/domain/order/query/OrderDetailResponseMapper.java`
- Modify: `core/core-api/src/main/java/com/ticket/core/domain/order/query/usecase/GetOrderDetailUseCase.java`
- Modify: `core/core-api/src/main/java/com/ticket/core/domain/order/command/usecase/CancelOrderUseCase.java`
- Modify: `core/core-api/src/main/java/com/ticket/core/domain/hold/command/usecase/CreateHoldUseCase.java`
- Modify: `core/core-api/src/main/java/com/ticket/core/api/controller/docs/HoldControllerDocs.java`
- Modify: `core/core-api/src/main/java/com/ticket/core/api/controller/docs/OrderControllerDocs.java`

- [ ] Hold 응답과 Location/X-Order-Id 헤더를 `orderKey` 로 바꾼다.
- [ ] 주문 조회/취소 path variable 과 use case input 을 `orderKey` 로 바꾼다.
- [ ] 주문 상세 응답의 식별자 필드를 `orderKey` 로 바꾼다.
- [ ] 문서 예시와 설명을 모두 `orderKey` 기준으로 갱신한다.
- [ ] 컴파일 확인: `./gradlew :core:core-api:compileJava`
- [ ] 커밋

## Chunk 2: 주문 시작과 상태 전이 단순화

### Task 3: 주문 시작 흐름을 단일 use case 로 정리

**Files:**
- Modify: `core/core-api/src/main/java/com/ticket/core/domain/hold/command/usecase/CreateHoldUseCase.java`
- Modify: `core/core-api/src/main/java/com/ticket/core/domain/order/application/CreateOrderApplicationService.java`
- Modify: `core/core-api/src/main/java/com/ticket/core/domain/hold/support/HoldRedisService.java`
- Modify: `core/core-api/src/main/java/com/ticket/core/domain/order/repository/OrderRepository.java`

- [ ] `CreateHoldUseCase` 를 내부적으로 “주문 시작” 흐름으로 정리한다.
- [ ] 회차당 `PENDING` 1건 정책을 use case 안에서 강제한다.
- [ ] `CreateOrderApplicationService` 는 주문/좌석/hold history 생성만 담당하게 축소한다.
- [ ] hold 생성 후 반환값이 `orderKey` 와 연결되도록 정리한다.
- [ ] 컴파일 확인: `./gradlew :core:core-api:compileJava`
- [ ] 커밋

### Task 4: 상태 전이를 단일 lifecycle 서비스로 모은다

**Files:**
- Modify: `core/core-api/src/main/java/com/ticket/core/domain/order/domainservice/OrderLifecycleDomainService.java`
- Create: `core/core-api/src/main/java/com/ticket/core/domain/order/application/OrderLifecycleApplicationService.java`
- Modify: `core/core-api/src/main/java/com/ticket/core/domain/order/command/usecase/CancelOrderUseCase.java`
- Modify: `core/core-api/src/main/java/com/ticket/core/domain/order/application/OrderExpirationApplicationService.java`
- Modify: `core/core-api/src/main/java/com/ticket/core/domain/order/application/OrderHoldExpirationApplicationService.java`
- Modify: `core/core-api/src/main/java/com/ticket/core/domain/order/command/usecase/ExpireOrderUseCase.java`

- [ ] 주문 취소/만료를 공통 lifecycle application service 로 위임한다.
- [ ] 서비스 한 곳에서 `Order`, `OrderSeat`, `HoldHistory` 전이를 함께 처리한다.
- [ ] `holdKey` 기반 만료와 `orderKey` 기반 만료가 같은 내부 로직을 타게 정리한다.
- [ ] 컴파일 확인: `./gradlew :core:core-api:compileJava`
- [ ] 커밋

## Chunk 3: 만료·해제 흐름 단일화

### Task 5: Redis listener 와 scheduler 를 공통 만료 진입점으로 수렴

**Files:**
- Modify: `core/core-api/src/main/java/com/ticket/core/domain/hold/event/RedisKeyExpirationListener.java`
- Modify: `core/core-api/src/main/java/com/ticket/core/domain/order/command/OrderExpirationScheduler.java`
- Modify: `core/core-api/src/main/java/com/ticket/core/domain/order/event/OrderTransactionEventListener.java`
- Modify: `core/core-api/src/main/java/com/ticket/core/domain/hold/application/HoldReleaseApplicationService.java`
- Modify: `core/core-api/src/main/java/com/ticket/core/domain/hold/support/HoldRedisService.java`

- [ ] Redis TTL listener 는 만료 감지만 하고 공통 lifecycle service 를 호출하게 바꾼다.
- [ ] 스케줄러도 같은 lifecycle service 를 호출하게 바꾼다.
- [ ] hold 해제는 `performanceId + holdKey + seatIds` 기반 한 경로로 통일한다.
- [ ] 주문 종료 이벤트는 좌석 상태 publish 용 후처리만 남긴다.
- [ ] 컴파일 확인: `./gradlew :core:core-api:compileJava`
- [ ] 커밋

### Task 6: 마무리 정리

**Files:**
- Modify: 영향 받은 문서/응답/컨트롤러 전반

- [ ] 남은 `orderId` 외부 노출, `holdKey` 오남용, 중복 상태 전이 경로를 검색해 제거한다.
- [ ] 최종 컴파일 확인: `./gradlew :core:core-api:compileJava`
- [ ] 기능별 커밋과 push 를 정리한다.
