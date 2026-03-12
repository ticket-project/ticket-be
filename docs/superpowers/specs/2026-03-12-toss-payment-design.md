# Toss Payment Design

> **For agentic workers:** 이 설계는 현재 `hold -> PENDING order` 구조 위에 토스 결제위젯 기반 즉시결제를 추가하기 위한 기준 문서다.

**Goal:** 현재 티켓팅 주문 흐름에 토스 결제위젯 기반 즉시결제를 도입해 `PENDING order` 를 실제 결제 완료와 연결한다.

**Current Context:**
- 주문 시작은 `StartOrderUseCase` 가 담당한다.
- 주문 종료는 `TerminateOrderUseCase` 가 담당한다.
- `Order` 는 현재 `PENDING/CONFIRMED/CANCELED/EXPIRED/PAYMENT_FAILED` 상태를 가진다.
- 결제 aggregate, 결제 API, 토스 연동 코드는 아직 없다.

---

## 1. 범위

이번 범위는 아래만 포함한다.

- 토스 결제위젯 기반 즉시결제
- 결제 준비 API
- 결제 승인 API
- 결제 실패 API
- 간단한 로컬 HTML 데모

이번 범위에서 제외한다.

- 가상계좌
- 웹훅 기반 보정
- 환불/부분취소
- 다중 PG 추상화
- 정기결제

## 2. 아키텍처

### Order
- 예매 주문 aggregate
- 좌석 hold 와 연결된 결제 대기 상태를 가진다
- 결제 성공 시 `CONFIRMED`
- 결제 실패 시 `PAYMENT_FAILED`

### Payment
- 결제 aggregate
- 주문과 1:1 관계
- 토스 인증 결과와 승인 결과를 저장한다
- 토스 전용 식별자인 `paymentKey` 를 가진다

### UseCase
- `PreparePaymentUseCase`
  - 결제 준비 진입점
  - `orderKey` 기반으로 결제 가능 여부 확인
  - 결제용 `Payment` 생성 또는 재사용
- `ConfirmPaymentUseCase`
  - 토스 인증 성공 후 서버 승인 처리
  - 내부 검증 후 토스 confirm API 호출
  - 성공 시 `Payment` 와 `Order` 상태 확정
- `FailPaymentUseCase`
  - 토스 실패 URL 또는 프론트 실패 처리 진입점
  - `Payment` 와 `Order` 를 실패 상태로 종료

### External Integration
- 토스 결제위젯은 프론트에서 호출한다.
- 백엔드는 토스 confirm API 만 직접 호출한다.

## 3. 상태 머신

### Order
- `PENDING`
  - hold 성공 직후 생성
- `CONFIRMED`
  - 결제 승인 성공
- `PAYMENT_FAILED`
  - 결제 승인 실패 또는 명시적 실패 처리
- `CANCELED`
  - 사용자 취소
- `EXPIRED`
  - hold 만료

전이 규칙:
- `PENDING -> CONFIRMED`
- `PENDING -> PAYMENT_FAILED`
- `PENDING -> CANCELED`
- `PENDING -> EXPIRED`

### Payment
- `READY`
  - 결제 준비 완료
- `AUTHENTICATED`
  - 토스 인증 완료, 아직 서버 승인 전
- `APPROVED`
  - 토스 승인 성공
- `FAILED`
  - 토스 승인 실패 또는 실패 URL 처리
- `CANCELED`
  - 차후 환불/취소 확장용

## 4. 핵심 시퀀스

### 4.1 결제 준비
1. 프론트가 `orderKey` 로 결제 준비 API 호출
2. 서버가 주문 소유자, 상태 `PENDING`, 만료 여부, 금액을 검증
3. 서버가 `Payment` 를 생성하거나 기존 미완료 결제를 재사용
4. 서버가 프론트에 토스 위젯 구동에 필요한 값 반환

### 4.2 결제 승인
1. 프론트가 토스 위젯으로 결제 인증
2. 성공 URL에서 `paymentKey`, `orderId`, `amount` 획득
3. 프론트가 서버 결제 승인 API 호출
4. 서버가 `orderKey == orderId`, 금액 일치, 주문 상태, 만료 여부 검증
5. 서버가 토스 confirm API 호출
6. 승인 성공 시:
   - `Payment.APPROVED`
   - `Order.CONFIRMED`
   - `OrderSeat.CONFIRMED`
   - `HoldHistory.CONFIRMED`
   - `PerformanceSeat.RESERVED`
   - hold 해제 및 좌석 확정 이벤트 발행

### 4.3 결제 실패
1. 프론트가 실패 URL에서 실패 코드/메시지 수신
2. 서버 실패 API 호출
3. 서버가 `Payment.FAILED`, `Order.PAYMENT_FAILED` 처리
4. hold 해제 및 좌석 released 이벤트 발행

## 5. 데이터 설계

### Payment 필드 초안
- `id`
- `paymentKey` 내부 결제 식별자
- `orderId` 내부 FK
- `provider` (`TOSS`)
- `providerPaymentKey` 토스 `paymentKey`
- `amount`
- `status`
- `method`
- `approvedAt`
- `failedAt`
- `failureCode`
- `failureMessage`
- `rawResponse` 또는 요약 payload 필드

설계 원칙:
- 외부 주문 식별은 `orderKey`
- 외부 결제 식별은 토스 `paymentKey`
- 내부 PK 는 별도 유지

## 6. 검증 규칙

승인 전에 반드시 확인한다.

- 주문이 본인 소유인지
- 주문 상태가 `PENDING` 인지
- 주문이 만료되지 않았는지
- 요청 금액과 주문 금액이 일치하는지
- 같은 주문이 이미 승인 완료되지 않았는지

실패 시 우선순위:
- 이미 `CONFIRMED` 면 멱등 응답
- `EXPIRED/CANCELED/PAYMENT_FAILED` 면 승인 불가
- 토스 승인 실패면 내부 결제 실패 처리

## 7. 프론트 데모 범위

간단한 HTML 페이지 하나로 아래를 수행한다.

- `orderKey` 입력
- 결제 준비 API 호출
- 토스 위젯 렌더링
- 결제 요청
- 성공 시 승인 API 호출
- 실패 시 실패 API 호출
- 응답 결과 표시

목적:
- 로컬에서 결제 흐름을 손으로 검증
- 프론트 협업용 샘플 제공

## 8. 향후 확장

이번 설계 이후 확장 포인트:
- `PaymentProvider` 인터페이스 도입
- 토스 외 PG 추가
- 웹훅 기반 보정
- 환불/부분취소
- 대기열과 결제 준비 연동

