# Ticket Codex 리뷰 가이드

이 문서는 루트 [AGENTS.md](../AGENTS.md)를 보완하는 Codex 전용 리뷰 지침이다.

## 문서 우선순위

아래 순서로 저장소 의도를 파악한다.

1. `AGENTS.md`
2. `docs/development.md`
3. `docs/architecture.md`
4. `docs/operations.md`
5. `settings.gradle`, 각 모듈 `build.gradle`
6. 관련 테스트 코드

## 리뷰 기본 원칙

- 리뷰, 요약, 코멘트, 제안은 항상 한국어로 작성한다.
- PR 리뷰는 심층조사 포함 모드로 수행한다.
- 패치만 보지 말고 주변 코드, 호출 흐름, 관련 설정, 관련 테스트까지 함께 읽는다.
- 취향성 스타일 지적보다 실제 결함 가능성, 회귀 위험, 테스트 공백을 우선한다.
- 요청 범위를 벗어난 기능 추가, 리팩터링, 추상화 제안은 최소화한다.

## 상시 심층리뷰 기준

1. 변경 파일과 모듈 경계를 먼저 확인한다.
2. 패치와 함께 주변 코드, 호출자, 피호출자, 관련 테스트를 읽는다.
3. `core-api`, `core-domain`, `core-infra` 경계 위반 여부를 본다.
4. 보안, 동시성, 트랜잭션, Redis TTL/만료, 테스트 공백을 점검한다.
5. 근거가 약한 코멘트는 남기지 않는다.

## 핵심 위험 지점

- `auth`: JWT, refresh token, OAuth2, 보안 필터 체인, 공개 API 노출
- `hold`, `order`: 주문 시작/취소/만료와 hold 해제 일관성, 부분 상태 전이
- `performanceseat`: selection/hold 충돌, 좌석 상태 계산, 실시간 브로드캐스트
- `queue`: 토큰, TTL, 만료 처리, admitted/waiting 상태 전이
- Redis / Redisson: key naming, TTL, expiration listener, scheduler, 락 범위

## 권장 검증 명령

```bash
./gradlew :core:core-api:compileJava
./gradlew :core:core-domain:test
./gradlew :core:core-api:test
./gradlew :core:core-domain:test --tests "com.ticket.core.domain.CoreDomainArchitectureTest"
./gradlew :core:core-domain:test --tests "com.ticket.core.domain.CoreDomainModuleStructureTest"
```

## 출력 형식

- findings first 원칙을 따른다.
- 심각도 높은 순서로 적는다.
- 각 이슈는 왜 문제인지, 어떤 조건에서 깨지는지, 어디를 봐야 하는지 짧게 적는다.
- 치명적 문제가 없으면 그 사실을 명시하고, 남은 검증 공백만 짧게 덧붙인다.
