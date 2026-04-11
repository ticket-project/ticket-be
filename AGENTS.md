# Ticket — Codex Agent & Review Guidelines

> 이 파일은 OpenAI Codex가 읽는 에이전트 지침입니다.
> 전체 프로젝트 맵은 [CLAUDE.md](CLAUDE.md)를 참고하세요.

## 프로젝트 개요

실시간 좌석 선택, 대기열, 분산 락을 활용하는 티켓 예매 시스템 (Spring Boot 4 + Redis + Oracle).
모듈: core-api (HTTP 진입점) / core-domain (비즈니스 로직) / redis-core (Redis 설정).

## Review guidelines

모든 리뷰 코멘트는 한국어로 작성하고, 반드시 심각도 태그를 앞에 붙이세요.

### 심각도 분류

- `[P0-Bug]`: 버그, 데이터 손실, 보안 취약점 — 반드시 수정
- `[P0-Arch]`: 아키텍처 위반 — 반드시 수정
  - core-api에 비즈니스 규칙 또는 저장소 접근이 있는 경우
  - infra 패키지 밖에서 Redisson, Spring Data Redis, Spring Messaging 직접 참조
  - core-domain에 JWT, Swagger, Response DTO, CookieUtils가 있는 경우
- `[P1-Concurrency]`: 동시성, 분산 락, TTL 정합성 — 수정 권장
  - 홀드 생성 시 좌석별 분산 락 누락
  - Redis TTL 만료 후 DB 측 정합성 처리 누락
  - @TransactionalEventListener(AFTER_COMMIT) 미사용
- `[P1-Test]`: 테스트 누락, 기존 테스트 불일치 — 수정 권장
- `[P2-Suggestion]`: 설계 개선, 네이밍 — 판단 필요 (최소한으로)

P2-Style (순수 스타일/포맷팅) 코멘트는 남기지 마세요.

### 코멘트 형식

```
[P1-Concurrency] 분산 락 범위 부족

이 메서드에서 좌석별 분산 락 없이 홀드를 생성하고 있습니다.
동시에 두 사용자가 같은 좌석에 홀드를 시도하면 이중 점유가 발생합니다.

수정: HoldManager.createHold() 호출 전에 좌석별 분산 락을 획득하세요.
참고: docs/RELIABILITY.md
```

## 아키텍처 규칙

계층 규칙: `Types → Config → Repo → Service → Runtime → UI` (단방향)

```
core-api  ──depends──▶  core-domain  ──depends──▶  redis-core
```

- core-domain은 순수 비즈니스 로직만 포함
- core-api 소유: JWT, Swagger, Response DTO, CookieUtils, Controller
- infra 격리: Redisson, Spring Data Redis, Spring Messaging → `..infra..` 패키지에서만

상세: [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)

## 동시성 규칙

- 분산 락: 홀드(좌석별 15초), 대기열(공연별 5초)
- Redis TTL 만료 시 DB 정합성 처리 필수
- 커밋 전 Redis 변경 금지 → @TransactionalEventListener(AFTER_COMMIT)
- 홀드 해제는 Outbox 패턴으로 최종 일관성 보장

상세: [docs/RELIABILITY.md](docs/RELIABILITY.md)

## 도메인별 주의 지점

| 도메인 | 핵심 검토 항목 |
|---|---|
| hold | 분산 락, TTL, 이중 점유, Outbox 해제 |
| order | 상태 전이 (PENDING→terminal), 만료 스케줄러, 홀드 연동 |
| queue | 토큰 TTL, 입장 승격 동시성, maxActiveUsers |
| performanceseat | 좌석 상태 계산, selection/hold 충돌, WebSocket |
| auth | JWT, refresh token, OAuth2, security filter chain |

## 검증 명령어

```bash
./gradlew :core:core-api:compileJava     # 빠른 컴파일 체크
./gradlew :core:core-domain:test          # 아키텍처 + 도메인 테스트
./scripts/verify-fix.sh                   # 전체 검증 파이프라인
```

## 설계 원칙

상세: [docs/design-docs/core-beliefs.md](docs/design-docs/core-beliefs.md)
