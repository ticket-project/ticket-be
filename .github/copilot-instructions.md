# Ticket 저장소 Copilot 지침

이 저장소에서 Copilot Chat, Copilot code review, Copilot coding agent는 아래 규칙을 우선 따른다.

## 기본 원칙

- 리뷰, 제안, 설명은 항상 한국어로 작성한다.
- 이 저장소는 에이전트가 리뷰를 읽고 자동 수정하는 워크플로우를 사용한다.
- 모든 리뷰 코멘트 앞에 **심각도 태그를 필수로** 붙인다.
- 요청 범위를 벗어난 기능 추가, 리팩터링, 추상화는 제안하지 않는다.
- 변경은 수술적으로 한다. 현재 PR의 목표와 직접 관련 없는 파일은 건드리지 않는다.
- 스타일 취향 코멘트(P2-Style)는 남기지 않는다.

## 심각도 태그

모든 인라인 코멘트/이슈 앞에 아래 태그 중 하나를 붙인다:

| 태그 | 의미 | 에이전트 행동 |
|---|---|---|
| `[P0-Bug]` | 버그, 데이터 손실, 보안 취약점 | 반드시 수정 |
| `[P0-Arch]` | 아키텍처 위반 (모듈 경계, 인프라 격리) | 반드시 수정 |
| `[P1-Concurrency]` | 동시성, 분산 락, TTL 정합성 | 수정 권장 |
| `[P1-Test]` | 테스트 누락, 기존 테스트 불일치 | 수정 권장 |
| `[P2-Suggestion]` | 설계 개선, 네이밍, 리팩터링 제안 | 판단 필요 |

## 문서 우선순위

아래 순서로 저장소 의도를 파악한다.

1. `CLAUDE.md` — 프로젝트 맵, 계층 규칙, 스킬, 검증 명령어
2. `docs/ARCHITECTURE.md` — 모듈 의존 방향, 인프라 격리 규칙
3. `docs/RELIABILITY.md` — 분산 락, TTL, 트랜잭션 이벤트 규칙
4. `docs/design-docs/core-beliefs.md` — 설계 원칙
5. 해당 모듈의 `build.gradle`, `settings.gradle`, 테스트 코드

## 모듈 경계

- `core/core-api`는 HTTP/WebSocket 진입점과 설정에 집중한다.
- `core/core-api`에 비즈니스 규칙이나 직접적인 저장소 접근 로직을 넣지 않는다 → 위반 시 `[P0-Arch]`.
- `core/core-domain`은 비즈니스 규칙 중심 모듈이다.
- Redisson, Spring Data Redis, Spring Messaging은 `..infra..` 패키지에서만 사용한다 → 위반 시 `[P0-Arch]`.
- JWT, Swagger, Response DTO, CookieUtils는 core-api에만 존재한다 → 위반 시 `[P0-Arch]`.

## 리뷰 우선순위

P0/P1만 집중하고 P2는 최소한으로:

- `[P0-Bug]` 버그, 회귀, 누락된 예외 흐름, 인증/인가 누락, 공개 API 노출
- `[P0-Arch]` 모듈 경계 위반, 인프라 격리 위반 (docs/ARCHITECTURE.md 기준)
- `[P1-Concurrency]` 동시성 문제, 분산 락 누락, 트랜잭션 경계, TTL/만료 정합성 (docs/RELIABILITY.md 기준)
- `[P1-Test]` 테스트 누락, JPA/Querydsl 조회 이상, N+1

## 도메인별 주의 지점

- `auth`: JWT, refresh token, OAuth2, security filter chain
- `hold`, `order`: 상태 전이 일관성, 홀드 해제 Outbox 패턴
- `performanceseat`: 좌석 상태 계산, selection/hold 충돌, WebSocket 브로드캐스트
- `queue`: 토큰 TTL, 만료 처리, 입장 승격 동시성

## 권장 검증

- 빠른 검증: `./gradlew :core:core-api:compileJava`
- 구조 및 도메인 검증: `./gradlew :core:core-domain:test`
- 전체 검증: `./scripts/verify-fix.sh`

## 리뷰 코멘트 형식

에이전트가 파싱하기 쉽도록 다음 형식을 따른다:

```
[P1-Concurrency] 분산 락 범위 부족

이 메서드에서 좌석별 분산 락 없이 홀드를 생성하고 있습니다.
동시에 두 사용자가 같은 좌석에 홀드를 시도하면 이중 점유가 발생합니다.

수정: HoldManager.createHold() 호출 전에 좌석별 분산 락을 획득하세요.
참고: docs/RELIABILITY.md#분산-락-규칙
```
