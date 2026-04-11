# Reliability — 동시성 및 신뢰성 규칙

## 분산 락 규칙

| 대상 | 락 키 | 리스 시간 | 용도 |
|---|---|---|---|
| 홀드 생성/해제 | 공연ID + 좌석ID | 15초 | 좌석 이중 점유 방지 |
| 대기열 진입/퇴장 | 공연ID | 5초 | 대기열 상태 경합 방지 |
| 좌석 선택/해제 | 공연ID + 좌석ID | - | 선택 경합 방지 |

**원칙**:
- 락 범위는 최소한으로 유지한다
- 락 내부에서 외부 API 호출하지 않는다
- 락 획득 실패 시 적절한 예외를 던진다 (무한 재시도 금지)

## Redis TTL 관리

| 데이터 | TTL | 만료 처리 |
|---|---|---|
| 좌석 선택 | 5분 (고정) | Redis 자동 만료 |
| 홀드 | 공연 설정값 (holdTime) | Redis TTL + Outbox 스케줄러 |
| 대기열 토큰 | QueueLevel별 (`entryTokenTtl`) | Redis 자동 만료 |

**원칙**:
- TTL이 있는 데이터는 만료 후 처리 로직이 반드시 존재해야 한다
- Redis 키 네이밍: `{도메인}:{공연ID}:{식별자}` 형태를 따른다
- TTL 값은 하드코딩하지 않고 설정(application.yml 또는 DB)에서 가져온다

## 트랜잭션 이벤트 규칙

```java
// DB 트랜잭션 커밋 후에만 Redis/WebSocket 처리
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)

// 롤백 시 보상 로직
@TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
```

**원칙**:
- DB 커밋 전에 Redis 상태를 변경하지 않는다
- Redis 실패가 DB 트랜잭션을 롤백시키면 안 된다
- 커밋 후 이벤트 실패는 Outbox 패턴으로 보상한다

## Outbox 패턴 (홀드 해제)

```
[Order 상태 변경] → [Outbox 레코드 생성] → (같은 트랜잭션)
                                              ↓
                      (AFTER_COMMIT) → [HoldReleaseOutboxExecutor] → [Redis 홀드 삭제]
                                              ↓ (실패 시)
                      [HoldReleaseOutboxScheduler] → 재시도 (지수 백오프)
```

**상태**: PENDING → COMPLETED / FAILED
- `retryCount` 추적
- `nextAttemptAt` 기반 재시도
- 오류 메시지 최대 1000자 기록

## 상태 머신 규칙

### 주문 (Order)
```
PENDING ──▶ CONFIRMED  (결제 성공)
PENDING ──▶ EXPIRED    (TTL 만료)
PENDING ──▶ CANCELED   (사용자 취소)
PENDING ──▶ PAYMENT_FAILED (결제 실패)
```
- PENDING 외의 상태에서는 상태 변경 불가 (terminal state)

### 홀드 (Hold)
```
ACTIVE ──▶ CONFIRMED  (주문 확정)
ACTIVE ──▶ EXPIRED    (TTL 만료)
ACTIVE ──▶ CANCELED   (주문 취소)
```

### 대기열 (Queue)
```
WAITING ──▶ ADMITTED  (입장 허용)
ADMITTED ──▶ EXPIRED  (토큰 만료)
ADMITTED ──▶ LEFT     (자진 퇴장)
```

## 장애 시나리오 체크리스트

PR 작성 시 다음을 검토한다:

- [ ] Redis 연결 끊김 시 DB 트랜잭션은 안전한가?
- [ ] 분산 락 획득 실패 시 사용자에게 적절한 응답을 반환하는가?
- [ ] TTL 만료와 수동 해제가 동시에 발생할 때 멱등한가?
- [ ] 스케줄러 중복 실행 시 데이터 정합성이 유지되는가?
- [ ] WebSocket 브로드캐스트 실패가 비즈니스 로직에 영향을 주지 않는가?
