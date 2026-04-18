# Ticket 저장소 Copilot 지침

이 저장소에서 Copilot Chat, Copilot code review, Copilot coding agent는 아래 규칙을 우선 따른다.

## 기본 원칙

- 리뷰, 제안, 설명은 항상 한국어로 작성한다.
- 모든 리뷰는 심층 리뷰로 수행한다. 패치만 보지 말고 주변 코드, 관련 설정, 관련 테스트, 호출 흐름을 함께 본다.
- 요청 범위를 벗어난 기능 추가, 리팩터링, 추상화는 제안하지 않는다.
- 변경은 수술적으로 한다. 현재 PR의 목표와 직접 관련 없는 파일은 건드리지 않는다.
- 스타일 취향보다 실제 결함 가능성, 회귀 위험, 테스트 공백을 우선 본다.

## 문서 우선순위

아래 순서로 저장소 의도를 파악한다.

1. `AGENTS.md`
2. `README_CODEX.md`
3. `README_ARCHITECTURE.md`
4. 해당 모듈의 `build.gradle`, `settings.gradle`, 테스트 코드
5. `README.md`, `README_JUNIE.md`, `README_PRODUCT.md`

## 모듈 경계

- `core/core-api`는 HTTP/WebSocket 진입점과 설정에 집중한다.
- `core/core-api`에 비즈니스 규칙이나 직접적인 저장소 접근 로직을 넣지 않는다.
- `core/core-domain`은 비즈니스 규칙 중심 모듈이다.
- Redis, Redisson, expiration listener, distributed lock 같은 기술 세부 구현은 가능한 한 도메인 `store` 또는 `infra`에 둔다.
- 모듈 경계나 패키지 구조를 건드리면 `:core:core-business:test` 기준의 구조 규칙을 우선 확인한다.

## 리뷰 우선순위

다음 항목을 우선 검토한다.

- 버그 가능성, 회귀 위험, 누락된 예외 흐름
- 동시성 문제, 분산 락 누락, 트랜잭션 경계 오류
- Redis TTL, key naming, expiration listener, hold/order 만료 처리 누락
- 인증, 인가, 토큰 처리, 공개 API 노출 위험
- API 호환성 변경과 응답 스키마 영향
- 테스트 누락 또는 기존 테스트와 불일치
- JPA/Querydsl 조회 이상, N+1, fetch 전략 문제

## 도메인별 주의 지점

- `auth`: JWT, refresh token, OAuth2, security filter chain
- `hold`, `order`: 주문 시작, 만료, 취소, hold 해제의 일관성
- `performanceseat`: 좌석 상태 계산, selection/hold 충돌, 실시간 브로드캐스트
- `queue`: 토큰, TTL, 만료 처리

## 권장 검증

- 빠른 검증: `./gradlew :core:core-api:compileJava`
- 구조 및 도메인 검증: `./gradlew :core:core-business:test`

## 리뷰 스타일

- 문제가 명확할 때만 지적한다.
- 문제를 적을 때는 왜 문제가 되는지, 어떤 시나리오에서 깨지는지 함께 설명한다.
- 단순 취향 차이는 강한 표현으로 지적하지 않는다.
- findings first 원칙을 따르고, 심각도 높은 순서로 정리한다.
