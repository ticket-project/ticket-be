### 선착순 티켓 예매 서비스 (TDD 중심) README

#### 개요
- 목표: 선착순 티켓 예매를 공정하고 안정적으로 처리하는 백엔드 서비스 설계. 본 문서는 코드 없이 TDD 관점에서 요구사항, 설계, API, 데이터 모델, 동시성 전략, 테스트 전략, 운영·보안, 백로그를 정리합니다.
- 기술 스택(가정): Spring Boot, Spring Data JPA, RDB(PostgreSQL/MySQL), Redis(Cache/Queue), 외부 PG, JWT
- KPI: 과판매 0, P95(<300ms, 큐 진입 제외), 실패율 <0.5%, 거래 성공률 >85%

---

### 목차
- [비즈니스 요구사항](#비즈니스-요구사항)
- [사용자 스토리와 수용 기준](#사용자-스토리와-수용-기준)
- [비기능 요구사항](#비기능-요구사항)
- [선착순 보장 전략](#선착순-보장-전략)
- [시스템 아키텍처](#시스템-아키텍처)
- [API 명세(초안)](#api-명세초안)
- [데이터 모델(초안)](#데이터-모델초안)
- [TDD 전략과 테스트 설계](#tdd-전략과-테스트-설계)
- [운영/관측성/알림](#운영관측성알림)
- [보안/규정](#보안규정)
- [프로젝트 운영 계획](#프로젝트-운영-계획)
- [백로그/마일스톤](#백로그마일스톤)
- [TDD 실행 순서 체크리스트](#tdd-실행-순서-체크리스트)
- [부록: 예시 테스트 케이스 명세](#부록-예시-테스트-케이스-명세)

---

### 비즈니스 요구사항
- 핵심 가치: 한정 수량 티켓을 선착순으로 공정하게 판매하며, 과판매(oversell) 없이 빠른 사용자 경험 제공
- 성공 지표(KPI)
  - 과판매 0건
  - 99% 요청 P95 < 300ms(큐 대기 제외), 전체 실패율 < 0.5%
  - 예약 토큰 발급 → 결제 완료 성공률 > 85%
- 트래픽/제약(가정)
  - 피크: 읽기 5k RPS, 예약 500 RPS, 결제 콜백 100 RPS
  - 초기: 단일 서비스 + RDB + Redis, 외부 PG 연동

---

### 사용자 스토리와 수용 기준
1. 회원가입/로그인
   - 이메일/비밀번호로 가입·로그인
   - 수용: 이메일 중복 방지, 비밀번호 정책(≥8자, 대소문자/숫자), JWT 발급
2. 이벤트/세션 조회
   - 이벤트, 세션(회차), 남은 재고 조회
   - 수용: 캐시 반영, P95 < 50ms, 재고 정확성 ±0
3. 대기열 토큰 발급(큐)
   - 오픈 이후 토큰 요청, 순번/만료 포함
   - 수용: 큐 정책 준수, TTL(예: 2분), 재고 0이면 대기 또는 실패
4. 홀딩(임시 재고 차감)
   - 토큰 소지자가 수량/좌석을 홀딩
   - 수용: 과판매 방지, 중복 홀딩 차단, TTL(예: 2분)
5. 결제 진행/완료
   - PG 연동, 콜백 멱등, 성공 시 SOLD, 실패/타임아웃 시 해제
6. 마이티켓/영수증
   - 결제 완료 후 티켓 목록/QR 발급
7. 어드민
   - 이벤트 등록, 수량 설정, 판매 상태 관리, 모니터링

---

### 비기능 요구사항
- 성능: 캐시/쓰기 경로 최적화, 큐 기반 피크 흡수
- 신뢰성: 과판매, 중복 결제 0, 멱등 처리
- 보안: 비밀번호 해시(Argon2/bcrypt), JWT, HTTPS, 비밀관리
- 감사/로깅: 모든 거래 단계 감사 로그, 추적 ID
- 관측성: 메트릭·로그·트레이싱 기본 제공

---

### 선착순 보장 전략
- 대기열/큐
  - Redis Sorted Set 기반 토큰 발급(스코어 = 요청시각/증분카운터)
  - 토큰: {queueToken, position, expiresAt}, 허용 윈도우 내에서만 예약 가능
- 재고 상태 전이
  - AVAILABLE → HOLD(임시) → SOLD(결제완료)
  - HOLD는 TTL 만료 시 자동 복구
- 동시성 제어
  - 수량형: 조건부 업데이트(`remaining >= n`) 또는 Redis Lua 원자 차감 + 홀딩 기록
  - 좌석형: row-level 상태 전이 + 유니크 인덱스
  - 이중 안전장치: 차감 실패 즉시 실패 + 결제 확정 시 최종 검증
- 멱등/중복 보호
  - `Idempotency-Key` 기반 서버 저장, 동일 요청 재응답
  - 결제 콜백도 멱등 적용
- 백프레셔/스로틀링
  - 각 단계 레이트 리밋, 큐 길이 초과 시 빠른 실패

---

### 시스템 아키텍처
- 구성
  - Client → API Gateway(선택) → App(예약/결제/인증) → RDB + Redis(Cache/Queue) → PG → Logging/Monitoring
- 핵심 플로우
  1) 토큰 요청 → ZSET 삽입 → 허용 판단 → 토큰 반환
  2) 홀딩 요청 → 멱등 검증 → 원자 차감/좌석 전이 → 홀딩 레코드 + TTL → 응답
  3) 결제 시작/콜백 → 멱등 → HOLD→SOLD 전이 → 영수증 발급
- 장애/복구
  - 재기동 후 TTL 스캐너로 만료 처리
  - 콜백 지연/중복은 멱등으로 안전, 타임아웃 시 홀딩 해제

---

### API 명세(초안)
- 인증
  - POST `/api/auth/signup`
    - req: `{ "email": "a@b.com", "password": "Secret123" }`
    - res: 201 Created
  - POST `/api/auth/login`
    - req: `{ "email": "a@b.com", "password": "Secret123" }`
    - res: `{ "accessToken": "..." }`
- 조회
  - GET `/api/events`
  - GET `/api/events/{eventId}/sessions`
    - res: 세션, 남은수량, 판매상태(OPEN/CLOSED)
- 대기열
  - POST `/api/queue/token`
    - req: `{ "sessionId": "..." }`
    - res: `{ "queueToken": "...", "position": 123, "expiresAt": "ISO-8601" }`
  - GET `/api/queue/status?token=...`
    - res: `{ "position": 12, "allowed": true }`
- 예약(홀딩)
  - POST `/api/reservations/hold` (headers: `Idempotency-Key`)
    - req: `{ "sessionId": "...", "quantity": 2, "queueToken": "..." }`
    - or 좌석형: `{ "seatIds": ["A1","A2"], "queueToken": "..." }`
    - res: `{ "holdId": "...", "expiresAt": "ISO-8601" }`
  - DELETE `/api/reservations/hold/{holdId}` → 204
- 결제
  - POST `/api/payments/checkout`
    - req: `{ "holdId": "...", "method": "CARD" }`
    - res: `{ "paymentSessionUrl": "https://pg/..." }`
  - POST `/api/payments/callback`
    - req: `{ "pgPaymentId": "...", "status": "SUCCESS|FAIL" }`
    - res: 200 OK(멱등)
  - GET `/api/payments/{paymentId}`
- 마이티켓
  - GET `/api/me/tickets`
- 에러 규격 예시
  - 400_INVALID_ARGUMENT, 401_UNAUTHORIZED, 403_FORBIDDEN, 404_NOT_FOUND, 409_CONFLICT, 429_TOO_MANY_REQUESTS, 503_SERVICE_UNAVAILABLE

---

### 데이터 모델(초안)
- Users(id, email[unique], passwordHash, createdAt)
- Events(id, title, description, status)
- Sessions(id, eventId, startAt, totalStock, remainingStock, status)
- Seats(id, sessionId, number, status[AVAILABLE|HOLD|SOLD], holdId?)
- Holds(id, userId, sessionId, quantity or seatIds, expiresAt, status[ACTIVE|EXPIRED|RELEASED])
- Orders(id, userId, sessionId, holdId, amount, status[PENDING|PAID|CANCELLED], createdAt)
- Payments(id, orderId, pgPaymentId[unique], status, rawPayload, updatedAt)
- IdempotencyKeys(key[unique], scope, responseHash, createdAt, ttl)
- AuditLogs(id, actorId, action, targetType, targetId, meta, createdAt)
- 인덱스/제약
  - Sessions.remainingStock >= 0 체크, Orders.holdId unique, Payments.pgPaymentId unique, Seats(sessionId, number) unique

---

### TDD 전략과 테스트 설계
- 테스트 피라미드
  - 단위: 재고 전이, 멱등키, TTL 만료
  - 통합: JPA/트랜잭션, Redis 스크립트, 콜백 핸들러
  - E2E(선택): 핵심 경로 Happy + 경계/장애 소수
- Red-Green-Refactor 사이클 예시(수량형 홀딩)
  1) Red: 남은수량 5에서 3 홀딩 성공, 추가 3 홀딩 실패 테스트 작성
  2) Green: 원자 차감 최소 구현
  3) Refactor: 중복 제거, 에러 모델 정리, 멱등 도입
- 핵심 GWT 시나리오
  1) 남은수량 10 → 3 홀딩 → 남은 7, ACTIVE, 만료시간 설정
  2) 남은수량 2 → 3 홀딩 → 409_CONFLICT
  3) 동일 멱등키 재시도 → 동일 holdId 반환
  4) 홀딩 만료 → 스캐너 실행 → 재고 복구
  5) HOLD 상태 결제 성공 → SOLD 전이
  6) 콜백 중복 → 멱등으로 단일 SOLD 기록
  7) 큐 길이 초과 → 429 또는 대기 안내
- 성능/부하 테스트(예)
  - T0에 500 동시: 토큰 발급 → 100 RPS 홀딩 요청
  - 지표: 성공률, p50/p95, 큐 대기시간, 재시도율

---

### 운영/관측성/알림
- 로그: JSON 구조화, traceId/userId/action/outcome/latency
- 메트릭: queue.enqueue/allowed, hold.success/fail, payment.succeed/fail, inventory.remaining
- 트레이싱: 예약→결제 분산 트레이싱
- 알람: 결제 성공률 급락(<70%), hold.fail 급증, 허용율 급락, 잔여수량 음수

---

### 보안/규정
- 인증: JWT(Access/Refresh), 비밀번호 해시(Argon2/bcrypt), 이메일 검증(선택)
- 권한: USER/ADMIN 롤, 관리자 API 보호
- 입력 검증: 수량/좌석ID 범위, 멱등키 포맷
- CORS/CSRF: SPA 친화 CORS, 상태 변경은 JWT 필수
- 개인정보: 최소 수집, 보존·파기 정책 수립

---

### 프로젝트 운영 계획
- 브랜치: trunk + feature 브랜치, PR 리뷰
- 커밋: Conventional Commits(`docs:`, `test:`, `chore:` 등)
- CI(개요): PR 테스트 실행, 린트/정적분석(옵션), 배지
- 환경: local(H2/Redis), dev(test DB/Redis), prod(Postgres/Redis), 프로필 분리

---

### 백로그/마일스톤
- M1(핵심 선착순)
  - [ ] 이벤트/세션 조회(3)
  - [ ] 대기열 토큰 발급/상태(5)
  - [ ] 수량형 재고 홀딩/만료(8)
  - [ ] 결제 시작/콜백 멱등(8)
  - [ ] 주문 확정/티켓 발급(5)
  - [ ] 관측성 기본(로그/메트릭)(3)
- M2(좌석형/UX)
  - [ ] 좌석 모델/홀딩(8)
  - [ ] 마이티켓/QR(5)
  - [ ] 관리자 기능(8)
- M3(성능/안정성)
  - [ ] 부하 테스트/튜닝(8)
  - [ ] 장애 시나리오/복구 자동화(5)

---

### TDD 실행 순서 체크리스트
1. 도메인 규칙 명세서 작성(본 문서)
2. “수량형 재고 홀딩” 유스케이스부터 테스트 목록 작성
3. 단위 테스트 케이스(GWT) 확정
4. 실패 테스트(RED) 작성 → 최소 구현(GREEN) → 리팩터
5. 멱등/경계/에러 케이스 확장 → 통합 테스트 보강
6. 결제 콜백/멱등 → 주문 확정 순 확장
7. 큐/리밋/백프레셔 시뮬레이션 테스트

---

### 부록: 예시 테스트 케이스 명세
- DomainHoldServiceTest
  - `holdSucceedsWhenSufficientInventory`
  - `holdFailsWhenInsufficientInventory`
  - `holdIsIdempotentWithSameKey`
  - `holdExpiresAndRestoresInventory`
- PaymentCallbackHandlerTest
  - `paymentCompletionIsIdempotent`
  - `holdToSoldTransitionOnPaymentSuccess`
  - `paymentFailureReleasesHold`
- QueueServiceTest
  - `issuesTokenWithPosition`
  - `allowsWhenWithinWindow`
  - `rejectsWhenQueueIsFull`

---

본 문서는 코드 구현 없이 TDD를 위한 설계/명세를 정리한 문서입니다.
