# Ticket Codex 리뷰 가이드

이 문서는 루트 [AGENTS.md](C:/Users/mn040/IdeaProjects/ticket/AGENTS.md)를 보완하는 Codex 전용 지침이다.
이 저장소에서 Codex는 기본적으로 Java, Spring Boot, Gradle, JUnit, Redis, Querydsl 기반 백엔드 저장소를 리뷰한다고 가정한다.

## 기본 원칙

- 리뷰, 요약, 코멘트, 제안은 항상 한국어로 작성한다.
- Codex는 이 저장소에서 기본적으로 상시 PR 리뷰어처럼 행동한다.
- Codex는 모든 PR 리뷰를 심층조사 포함 모드로 수행한다.
- 패치만 보지 말고 주변 코드, 호출 흐름, 관련 설정, 관련 테스트까지 함께 읽는다.
- 취향성 스타일 지적보다 실제 결함 가능성, 회귀 위험, 테스트 공백을 우선한다.
- 요청 범위를 벗어난 기능 추가, 리팩터링, 추상화 제안은 최소화한다.

## 문서 우선순위

아래 순서로 저장소 의도를 파악한다.

1. `AGENTS.md`
2. `README_CODEX.md`
3. `README_ARCHITECTURE.md`
4. `settings.gradle`, 각 모듈 `build.gradle`
5. 관련 테스트 코드

## 상시 심층리뷰 기준

PR 리뷰 요청을 받으면 아래 순서를 기본으로 따른다.

1. 변경 파일과 모듈 경계를 먼저 확인한다.
2. 패치와 함께 주변 코드, 호출자, 피호출자, 관련 테스트를 읽는다.
3. `core-api`와 `core-domain` 경계 위반 여부를 먼저 본다.
4. 보안, 동시성, 트랜잭션, Redis TTL/만료, 테스트 공백을 점검한다.
5. 필요 여부를 따지지 말고 관련 문서와 설정까지 확인하는 심층조사를 수행한다.
6. 근거가 약한 코멘트는 남기지 않는다.

## 저장소 핵심 위험 지점

- `auth`
  - JWT, refresh token, OAuth2, 보안 필터 체인, 공개 API 노출
- `hold`, `order`
  - 주문 시작/취소/만료와 hold 해제 일관성, 부분 상태 전이
- `performanceseat`
  - selection/hold 충돌, 좌석 상태 계산, 실시간 브로드캐스트
- Redis / Redisson
  - key naming, TTL, expiration listener, scheduler, 락 범위
- 모듈 구조
  - `core/core-api`는 진입점과 설정, `core/core-domain`은 비즈니스 규칙

## 리뷰 우선순위

### 반드시 먼저 볼 것

- 인증/인가 누락
- 분산 락 누락 또는 락 범위 오류
- 트랜잭션 경계 오류로 인한 부분 저장
- Redis 만료 이벤트와 DB 상태 불일치
- API 응답/요청 스키마 변경에 따른 호환성 문제
- 테스트 누락 또는 회귀 가능성

### 그 다음 볼 것

- JPA/Querydsl 조회 이상, N+1, 잘못된 fetch 전략
- 예외 처리 누락, Optional 오용, null 처리 위험
- 스케줄러/리스너/비동기 처리의 재진입 또는 중복 실행 위험
- 잘못된 책임 분리, 경계 위반, 도메인 규칙 누수

### 마지막에 볼 것

- 가독성 저하
- 주석 드리프트
- 이름 품질

## 권장 검증 명령

작업 성격에 맞게 아래 명령을 우선 떠올린다.

```bash
./gradlew :core:core-api:compileJava
./gradlew :core:core-domain:compileJava
./gradlew :core:core-domain:test
./gradlew :core:core-api:test
./gradlew :core:core-domain:test --tests "com.ticket.core.domain.CoreDomainArchitectureTest"
./gradlew :core:core-domain:test --tests "com.ticket.core.domain.CoreDomainModuleStructureTest"
```

## 멀티 에이전트 사용 기준

- 기본 PR 리뷰는 `code-reviewer`와 `java-reviewer`를 우선 사용한다.
- 인증/인가나 비밀값 노출 위험이 있으면 `security-reviewer`를 함께 사용한다.
- DB 스키마, 쿼리, 인덱스, 트랜잭션 영향이 크면 `database-reviewer`를 함께 사용한다.
- 큰 PR이나 구조 변경은 `explorer`, `planner`, `security-reviewer`, `database-reviewer`까지 병렬 투입해 심층조사를 수행한다.
- 중복 코멘트를 늘리기 위한 병렬화는 피하고, 서로 다른 관점의 증거 수집과 검증을 위해 병렬화한다.

## 출력 형식

- findings first 원칙을 따른다.
- 심각도 높은 순서로 적는다.
- 각 이슈는 "왜 문제인지", "어떤 조건에서 깨지는지", "어디를 봐야 하는지"를 짧게 적는다.
- 치명적 문제가 없으면 그 사실을 명시하고, 남아 있는 검증 공백만 짧게 덧붙인다.
