# Hold Order Role Refactor Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** hold/order 내부 구조를 역할과 개념 연관성 기준으로 `StartOrderUseCase`, `TerminateOrderUseCase`, `HoldManager` 중심으로 재구성한다.

**Architecture:** 외부 API는 유지하고 내부 책임만 재배치한다. 주문 시작은 한 use case 안에서 좌석 검증, pending 주문 검증, hold 생성, pending order 생성을 처리하고, 주문 종료는 취소/만료를 한 use case 안에서 처리한다. Redis hold 저장소성 로직은 `HoldManager` 로 분리한다.

**Tech Stack:** Java, Spring Boot, Spring Data JPA, Redis/Redisson

---

## Chunk 1: 주문 시작 책임 재배치

### Task 1: HoldManager 도입

**Files:**
- Create: `core/core-api/src/main/java/com/ticket/core/domain/hold/support/HoldManager.java`
- Delete: `core/core-api/src/main/java/com/ticket/core/domain/hold/support/HoldRedisService.java`
- Modify: hold 생성/해제 호출부

- [ ] `HoldRedisService` 책임을 `HoldManager` 로 옮긴다.
- [ ] 생성/조회/해제 메서드 시그니처를 유지해 참조 교체 비용을 낮춘다.
- [ ] 컴파일 확인: `./gradlew :core:core-api:compileJava`

### Task 2: StartOrderUseCase 도입

**Files:**
- Create: `core/core-api/src/main/java/com/ticket/core/domain/order/command/usecase/StartOrderUseCase.java`
- Delete: `core/core-api/src/main/java/com/ticket/core/domain/hold/command/usecase/CreateHoldUseCase.java`
- Modify: `core/core-api/src/main/java/com/ticket/core/api/controller/HoldController.java`
- Modify: `core/core-api/src/main/java/com/ticket/core/api/controller/docs/HoldControllerDocs.java`

- [ ] 현재 `CreateHoldUseCase` 의 비즈니스를 `StartOrderUseCase` 로 이동한다.
- [ ] hold 생성은 내부 단계로만 남기고, use case 이름은 주문 시작을 드러내게 바꾼다.
- [ ] HoldController 는 `StartOrderUseCase` 하나만 호출하게 바꾼다.
- [ ] 컴파일 확인: `./gradlew :core:core-api:compileJava`

## Chunk 2: 주문 종료 책임 재배치

### Task 3: TerminateOrderUseCase 도입

**Files:**
- Create: `core/core-api/src/main/java/com/ticket/core/domain/order/command/usecase/TerminateOrderUseCase.java`
- Delete: `core/core-api/src/main/java/com/ticket/core/domain/order/application/OrderLifecycleApplicationService.java`
- Delete: `core/core-api/src/main/java/com/ticket/core/domain/order/command/usecase/CancelOrderUseCase.java`
- Modify: `core/core-api/src/main/java/com/ticket/core/api/controller/OrderController.java`
- Modify: `core/core-api/src/main/java/com/ticket/core/domain/hold/event/RedisKeyExpirationListener.java`
- Modify: `core/core-api/src/main/java/com/ticket/core/domain/order/command/OrderExpirationScheduler.java`

- [ ] 취소/만료 처리 로직을 `TerminateOrderUseCase` 로 옮긴다.
- [ ] Controller, listener, scheduler 는 이 use case 하나만 호출하게 바꾼다.
- [ ] `OrderLifecycleDomainService` 는 상태 전이 규칙만 유지한다.
- [ ] 컴파일 확인: `./gradlew :core:core-api:compileJava`

### Task 4: 종료 후처리 경로 정리

**Files:**
- Modify: `core/core-api/src/main/java/com/ticket/core/domain/order/event/OrderTransactionEventListener.java`
- Modify: `core/core-api/src/main/java/com/ticket/core/domain/hold/event/HoldTransactionEventListener.java`
- Modify: `core/core-api/src/main/java/com/ticket/core/domain/hold/support/HoldManager.java`
- Delete: `core/core-api/src/main/java/com/ticket/core/domain/hold/application/HoldReleaseApplicationService.java`

- [ ] hold 해제는 `HoldManager` 한 경로만 타게 바꾼다.
- [ ] 이벤트 리스너는 상태를 바꾸지 않고 release/publish 후처리만 담당하게 유지한다.
- [ ] 최종 컴파일 확인: `./gradlew :core:core-api:compileJava`
