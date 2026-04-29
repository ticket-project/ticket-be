# Product Sense — 티켓 예매 흐름

## 전체 사용자 여정

```
[공연 탐색] → [대기열 진입] → [좌석 선택] → [주문 생성] → [결제] → [예매 완료]
```

## Phase 1: 대기열 진입 (Queue)

사용자가 공연 예매 페이지에 접근하면 대기열에 진입한다.

```
JoinQueueUseCase.execute()
├── QueueReentryCleaner: 기존 대기열 항목 정리
├── QueuePolicyResolver: 공연별 대기열 정책 조회
└── QueueJoinProcessor.join()
    ├── 수용량 여유 → ADMITTED (즉시 입장, 토큰+TTL 발급)
    └── 수용량 초과 → WAITING (대기 순번 부여)
```

**상태 흐름**: WAITING → ADMITTED → EXPIRED / LEFT

**핵심 설정값**:
- `maxActiveUsers`: 동시 입장 가능 인원 (기본 300명)
- `entryTokenTtl`: 입장 토큰 유효 시간 (기본 10분)
- `QueueLevel`: 대기열 강도 (LEVEL_1, LEVEL_2)

## Phase 2: 좌석 선택 (PerformanceSeat)

입장한 사용자가 공연장 좌석 배치도에서 좌석을 선택한다.

```
SelectSeatUseCase.execute()
├── 좌석이 이미 홀드/선택 상태인지 검증
├── SeatSelectionService.select() → Redis에 저장 (TTL 5분)
└── SeatStatusPublisher → WebSocket으로 실시간 상태 브로드캐스트
```

**좌석 상태**:
| 상태 | 저장소 | 의미 |
|---|---|---|
| AVAILABLE | DB 기본값 | 예매 가능 |
| SELECTED | Redis (5분 TTL) | 사용자가 임시 선택 중 |
| HELD | Redis (홀드 TTL) | 주문 생성됨, 결제 대기 |
| RESERVED | DB 영구 | 결제 완료, 예매 확정 |

## Phase 3: 주문 생성 + 홀드 할당 (Order + Hold)

좌석 선택 후 주문을 생성하면 홀드가 동시에 할당된다.

```
CreateOrderUseCase.execute()
├── CreateOrderValidator: 회원/공연/좌석 수 검증, 중복 주문 방지
├── HoldAllocator.allocate()
│   ├── HoldSeatAvailabilityValidator: 좌석 미점유 확인
│   ├── HoldManager.createHold() → Redis에 홀드 저장 (분산 락)
│   └── holdKey + expiresAt 생성
├── OrderCreator.createPendingOrder()
│   ├── orderKey 생성, PENDING 상태
│   ├── OrderSeat 레코드 (좌석별 가격 스냅샷)
│   └── totalAmount 계산
├── HoldHistoryRecorder.recordCreated() → 감사 추적
└── HoldCreatedEvent 발행
    └── (AFTER_COMMIT) 좌석 선택 Redis에서 제거 + WebSocket HELD 브로드캐스트
```

**주문 상태**: PENDING → CONFIRMED / EXPIRED / CANCELED / PAYMENT_FAILED

**홀드 상태**: ACTIVE → CONFIRMED / EXPIRED / CANCELED

## Phase 4: 주문 만료 / 취소 시 홀드 해제

```
주문 상태 변경 (만료/취소)
  → HoldReleaseOutboxWriter.append() → Outbox 레코드 생성
  → HoldReleaseRequestedEvent 발행
  → (AFTER_COMMIT) HoldReleaseOutboxExecutor.process()
      → HoldManager.release() → Redis 홀드 삭제 (분산 락)
      → Outbox COMPLETED 마킹
```

**Outbox 패턴**: 트랜잭션 일관성 보장. 실패 시 스케줄러가 재시도.

## Phase 5: 백그라운드 스케줄러

| 스케줄러 | 주기 | 역할 |
|---|---|---|
| `OrderExpirationScheduler` | 5분 | 만료된 PENDING 주문 배치 처리 (100건씩) |
| `HoldReleaseOutboxScheduler` | 설정값 | 실패한 홀드 해제 재시도 |
| `QueueAdmissionAdvancer` | 이벤트 기반 | 퇴장 시 대기자 입장 승격 |

## 도메인 관계도

```
Show (1) ──── (N) Performance ──── (N) PerformanceSeat ──── (1) Seat
                     │
                     ├── (N) QueueTicket (대기열)
                     └── (N) Order ──── (N) OrderSeat
                                │
                                └── holdKey ──── HoldSnapshot (Redis)
                                                    │
                                                    └── (N) HoldHistory (감사 추적)
```
