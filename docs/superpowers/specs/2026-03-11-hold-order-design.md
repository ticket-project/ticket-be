# Hold Order Design

**목표:** Redis 기반 좌석 HOLD와 DB 기반 주문/이력 구조를 결합해, 단순하지만 운영 추적 가능한 주문 흐름을 만든다.

## 핵심 원칙
- HOLD 소유권 판정은 Redis가 담당한다.
- 주문은 DB에 `PENDING`으로 먼저 생성하고, 결제 성공 시 `CONFIRMED`로 전이한다.
- HOLD 만료 시 연결된 `PENDING` 주문은 자동 `EXPIRED` 처리한다.
- 주문 1건은 단일 `performanceId`만 포함한다.
- 엔티티는 도메인 바운더리별로 나누고, 다른 도메인 엔티티를 직접 참조하지 않고 식별자 중심으로 연결한다.

## 바운더리
- `hold`
  - Redis hold 상태
  - DB `seat_holds` 이력
- `order`
  - DB `ticket_orders`
  - DB `order_seats`
- `performanceseat`
  - 최종 판매 상태 `AVAILABLE/RESERVED`

## 상태 전이
- HOLD: `ACTIVE -> CONFIRMED | EXPIRED | CANCELED`
- ORDER: `PENDING -> CONFIRMED | EXPIRED | CANCELED | PAYMENT_FAILED`
- PERFORMANCE_SEAT: `AVAILABLE -> RESERVED`

## API
- `POST /api/v1/performances/{performanceId}/holds`
- `GET /api/v1/orders/{orderId}`
- `DELETE /api/v1/orders/{orderId}`
- `POST /api/v1/orders/{orderId}/confirm`

## 응답 분리
- `POST /api/v1/performances/{performanceId}/holds`
  - 본문에는 상세 주문 정보를 담지 않는다.
  - 생성된 주문 식별자는 응답 헤더 `X-Order-Id`로만 전달한다.
- `GET /api/v1/orders/{orderId}`
  - 주문/결제 화면 전용 `OrderDetailResponse`를 반환한다.
  - 공연명, 회차 시작 시각, 공연장명, 좌석 표시 문자열, 예매자 정보, 금액 요약을 포함한다.
- `OrderResponse`
  - 취소/확정처럼 상태 전이 결과를 가볍게 돌려주는 요약 응답으로만 유지한다.

## 운영 규칙
- 한 번의 hold 요청은 최대 4석이다.
- 이미 선점 중인 좌석은 다시 선점할 수 없다.
- 좌석 상태 조회는 DB `RESERVED`와 Redis `SELECT/HOLD`를 합산해 계산한다.
