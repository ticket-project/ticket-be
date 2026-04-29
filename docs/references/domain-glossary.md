# Domain Glossary — 도메인 용어 사전

## 핵심 도메인 용어

| 용어 | 영문 | 설명 |
|---|---|---|
| 공연 | Show | 공연 정보의 최상위 단위 (제목, 장르, 출연진, 기간) |
| 회차 | Performance | 특정 날짜/시간에 열리는 공연의 한 회 |
| 좌석 | Seat | 공연장의 물리적 좌석 (구역, 열, 번호) |
| 회차별 좌석 | PerformanceSeat | 특정 회차에 대한 좌석+가격 매핑 |
| 대기열 | Queue | 공연 예매 접근을 제어하는 가상 대기줄 |
| 대기열 티켓 | QueueTicket | 사용자의 대기열 진입 기록 (순번, 상태, 토큰) |
| 좌석 선택 | Seat Selection | 사용자가 좌석을 임시로 점유 (Redis, 5분 TTL) |
| 홀드 | Hold | 주문 생성 시 좌석을 일정 시간 독점 점유 (Redis TTL) |
| 홀드 스냅샷 | HoldSnapshot | 홀드의 불변 상태 기록 |
| 주문 | Order | 결제 전 예매 요청 기록 |
| 주문 좌석 | OrderSeat | 주문에 포함된 개별 좌석+가격 스냅샷 |

## 상태 용어

| 용어 | 컨텍스트 | 의미 |
|---|---|---|
| WAITING | 대기열 | 대기 중, 아직 입장 불가 |
| ADMITTED | 대기열 | 입장 허용됨, 토큰 발급 |
| AVAILABLE | 좌석 | 예매 가능 상태 |
| SELECTED | 좌석 | 사용자가 임시 선택 (5분) |
| HELD | 좌석 | 주문으로 점유됨 (홀드 TTL) |
| RESERVED | 좌석 | 결제 완료, 확정 |
| PENDING | 주문 | 생성됨, 결제 대기 |
| CONFIRMED | 주문/홀드 | 결제 완료, 확정 |
| EXPIRED | 전체 | TTL 만료로 자동 해제 |
| CANCELED | 주문/홀드 | 사용자 또는 시스템에 의한 취소 |

## 기술 용어

| 용어 | 설명 |
|---|---|
| 분산 락 | Redisson 기반, 동시 접근 시 데이터 정합성 보장 |
| Outbox 패턴 | DB 트랜잭션과 비동기 작업의 최종 일관성 보장 |
| TTL | Time To Live, Redis 키의 자동 만료 시간 |
| holdKey | 홀드를 식별하는 64자 고유 키 |
| orderKey | 주문을 식별하는 고유 키 |
| queueToken | 대기열 입장을 증명하는 토큰 |
| entryTokenTtl | 입장 토큰의 유효 시간 |
| holdTime | 회차별 좌석 홀드 유지 시간 |
| maxCanHoldCount | 1인당 최대 좌석 점유 수 |
| maxActiveUsers | 동시 입장 가능 최대 인원 |
