# Distributed Lock AOP Improvement Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 재시도 없는 DistributedLock AOP를 도메인 예외와 트랜잭션 경계에 맞게 보강하고 `StartOrderUseCase`, `HoldManager` 에 적용한다.

**Architecture:** 락 획득과 해제는 AOP가 담당하고, 비즈니스 메서드는 검증과 저장 로직만 담당한다. AOP는 `CoreException(ErrorType)` 기반으로 실패를 표현하고, `@Transactional` 메서드 바깥에서 락을 먼저 획득하도록 우선순위를 조정한다.

**Tech Stack:** Java, Spring AOP, Redisson, Spring Transaction

---

## Chunk 1: AOP 보강

### Task 1: DistributedLock 애노테이션 확장

**Files:**
- Modify: `core/core-api/src/main/java/com/ticket/core/aop/DistributedLock.java`

- [ ] 락 실패 시 사용할 `ErrorType` 과 사용자 메시지 옵션을 추가한다.
- [ ] 기본 대기/임대 시간 옵션은 유지한다.

### Task 2: DistributedLockAop 정리

**Files:**
- Modify: `core/core-api/src/main/java/com/ticket/core/aop/DistributedLockAop.java`
- Delete: `core/core-api/src/main/java/com/ticket/core/aop/AopForTransaction.java`

- [ ] `REQUIRES_NEW` 우회용 컴포넌트 호출을 제거하고 `joinPoint.proceed()` 로 직접 진행한다.
- [ ] 트랜잭션보다 먼저 락을 잡도록 aspect 우선순위를 지정한다.
- [ ] 락 실패와 인터럽트는 `CoreException(ErrorType)` 로 변환한다.

## Chunk 2: 직접 락 제거

### Task 3: StartOrderUseCase 전환

**Files:**
- Modify: `core/core-api/src/main/java/com/ticket/core/domain/order/command/usecase/StartOrderUseCase.java`

- [ ] `memberId + performanceId` 락을 `@DistributedLock` 으로 대체한다.
- [ ] `RedissonClient`, `RLock`, unlock 코드와 상수 중 직접 락 관련 부분을 제거한다.

### Task 4: HoldManager 전환

**Files:**
- Modify: `core/core-api/src/main/java/com/ticket/core/domain/hold/support/HoldManager.java`

- [ ] `createHold`, `release` 에 멀티락 AOP를 적용한다.
- [ ] `createSeatLock`, retry/backoff, unlock 관련 직접 락 코드를 제거한다.
- [ ] Redis 저장/삭제 로직만 남긴다.

### Task 5: 검증

**Files:**
- Modify: 영향 받은 참조 파일들

- [ ] 남은 직접 `getLock/tryLock/getMultiLock` 사용처를 검색해 정리한다.
- [ ] 컴파일 확인: `./gradlew :core:core-api:compileJava`
