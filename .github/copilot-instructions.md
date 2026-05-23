# Ticket 저장소 Copilot 지침

이 저장소에서 Copilot Chat, Copilot code review, Copilot coding agent는 루트 [AGENTS.md](../AGENTS.md)를 공통 기준으로 따른다.

## 문서 우선순위

1. `AGENTS.md`
2. `docs/development.md`
3. `docs/architecture.md`
4. `docs/operations.md`
5. 관련 모듈의 `build.gradle`, `settings.gradle`, 테스트 코드

## Copilot 전용 리뷰 기준

- 모든 리뷰, 제안, 설명은 한국어로 작성한다.
- 패치만 보지 말고 주변 코드, 관련 설정, 관련 테스트, 호출 흐름을 함께 본다.
- 요청 범위를 벗어난 기능 추가, 리팩터링, 추상화는 제안하지 않는다.
- 스타일 취향보다 실제 결함 가능성, 회귀 위험, 테스트 공백을 우선 본다.

## 우선 검토 영역

- `auth`: JWT, refresh token, OAuth2, security filter chain
- `hold`, `order`: 주문 시작, 만료, 취소, hold 해제의 일관성
- `performanceseat`: 좌석 상태 계산, selection/hold 충돌, 실시간 브로드캐스트
- `queue`: 토큰, TTL, 만료 처리, 상태 전이
- `core-api`와 `core-domain` 경계 위반
- Redis TTL, key naming, expiration listener, scheduler, distributed lock

## 권장 검증

- 빠른 검증: `./gradlew :core:core-api:compileJava`
- 구조 및 도메인 검증: `./gradlew :core:core-domain:test`
