# Ticket 저장소 작업 가이드

이 문서는 Codex/에이전트가 이 저장소를 다룰 때 자동으로 참조하는 루트 작업 기준이다.

## 기본 원칙

- 문서 작성과 기본 응답은 한국어로 작성한다.
- 파일 인코딩은 UTF-8, BOM 없이 유지한다.
- 기능 추가보다 현재 요청의 목표를 먼저 구체화한다.
  - 예: "기능 추가"보다 "특정 테스트 통과", "특정 API 수정", "문서 정리"
- 요청되지 않은 추상화, 대규모 리팩터링, 부가 에러 처리 추가는 피한다.
- 변경은 수술적으로 한다. 작업 범위 바깥 파일은 건드리지 않는다.
- 구조 변경이나 모듈 경계를 건드리면 `:core:core-domain:test` 검증을 먼저 떠올린다.

## 문서 우선순위

이 저장소에서 현재 기준 문서는 아래 순서로 본다.

1. `README_CODEX.md`
2. `README_ARCHITECTURE.md`
3. 해당 모듈의 `build.gradle`, `settings.gradle`, 테스트 코드
4. `README.md`, `README_JUNIE.md`, `README_PRODUCT.md`

`README.md`, `README_JUNIE.md`는 초안 또는 과거 설명이 섞여 있을 수 있으므로, 직접 그 문서를 수정하는 작업이 아니면 우선순위를 낮춘다.

## 저장소를 읽는 순서

처음 구조를 파악할 때는 보통 아래 흐름으로 읽는다.

1. `settings.gradle`
   - 멀티 모듈 경계를 확인한다.
2. `build.gradle`와 각 모듈 `build.gradle`
   - 실제 의존 방향과 실행 모듈을 확인한다.
3. `README_CODEX.md`, `README_ARCHITECTURE.md`
   - 현재 팀이 의도한 패키지 경계와 흐름을 확인한다.
4. `core/core-api`
   - 외부 진입점, 보안, HTTP/WebSocket 설정을 본다.
5. `core/core-domain`
   - 비즈니스 흐름, 유스케이스, 저장소, 도메인 포트를 본다.
6. `core/core-infra`
   - Redis listener, Redisson 구현, Querydsl/P6Spy 설정 같은 기술 어댑터를 본다.
7. `core/core-domain` 테스트
   - ArchUnit과 도메인 테스트로 실제로 강제되는 규칙을 확인한다.

## 최상위 폴더 구조

### `core/`

애플리케이션 본체다. 실행 모듈, business core 모듈, infra adapter 모듈이 함께 있다.

- `core/core-api`
  - Spring Boot 실행 모듈이다.
  - `src/main/java/com/ticket/core/api/controller`가 HTTP 진입점이다.
  - `src/main/java/com/ticket/core/config`는 보안, WebSocket, seed, 공통 설정을 둔다.
  - `src/main/resources/application*.yml`이 실행 프로파일을 정의한다.
  - `src/main/resources/data.sql`은 로컬/H2 시드 데이터의 시작점이다.
  - 관계:
    - Controller는 직접 DB를 다루지 않고 `core-domain`의 use case, service, repository 조합을 호출한다.
    - `config/security`는 인증 정보를 해석해 Controller와 WebSocket 계층에 전달한다.
    - Swagger 문서용 타입은 `api/controller/docs`에, 요청 DTO는 `api/controller/request`에 모여 있다.

- `core/core-domain`
  - 비즈니스 규칙이 모이는 중심 모듈이다.
  - 패키지 기준은 기능 중심이다. 현재 주요 도메인은 `auth`, `hold`, `member`, `order`, `performance`, `performanceseat`, `queue`, `seat`, `show`, `showlike`, `commoncode`다.
  - 각 도메인 안에서 자주 보이는 하위 폴더 역할:
    - `command`: 상태 변경 흐름
    - `query`: 조회 흐름
    - `model`: 엔티티, 도메인 모델, 조회 모델
    - `repository`: JPA/RDB 중심 접근
    - `store`: Redis 등 상태 저장 추상화
    - `support`: 도메인 보조 컴포넌트
    - `infra`: 외부 기술 상세 구현
  - 관계:
    - `core-api`의 Controller는 여기 있는 command/query 흐름을 호출한다.
    - `repository`는 영속 저장소를, `store`는 Redis 같은 임시 상태 저장소를 다룬다.
    - `infra`는 아직 business core 안에 남아 있는 기술 구현 위치이고, 공통/실행 기술은 점진적으로 `core/core-infra`로 이동한다.
    - `performanceseat`는 좌석 선택/홀드/실시간 상태 반영이 얽혀 있어 `repository`, `store`, `support`, `infra`가 함께 동작한다.
    - `order`는 주문 상태를 들고, `hold`는 주문 시작 전후의 Redis hold 수명주기와 연결된다.
    - `queue`는 대기열 토큰과 만료 처리에 연결된다.

- `core/core-infra`
  - `core/core-domain`의 포트를 구현하는 기술 어댑터 모듈이다.
  - `src/main/java/com/ticket/core/infra`에는 Querydsl, P6Spy, 분산락 AOP, Redis expiration listener 같은 공통 인프라가 있다.
  - `src/main/java/com/ticket/core/domain/*/infra`에는 Redisson store, WebSocket seat event publisher 같은 도메인별 기술 구현이 있다.
  - 관계:
    - `core-api`가 이 모듈을 함께 로딩해 실행 시 필요한 bean을 조립한다.
    - `core-domain`은 포트와 유스케이스를 유지하고, 이 모듈이 실제 기술 구현을 제공한다.

- `core/build/`
  - Gradle 산출물이다. 소스가 아니라 결과물이다.
  - 수정 대상이 아니다.

### `storage/`

저장소 기술별 공통 자원을 둔다.

- `storage/redis-core`
  - Redis 관련 공통 의존성 모듈이다.
  - 현재는 `spring-boot-starter-data-redis`, `redisson-spring-boot-starter`를 제공한다.
  - 관계:
    - `core/core-domain`이 이 모듈을 의존해서 Redis/Redisson 기능을 사용한다.
    - 실제 비즈니스 Redis key 설계와 접근 구현은 `core/core-domain`의 각 도메인 `infra` 또는 `store`에 있다.

- `storage/images`
  - 이미지 원본 또는 정적 자산 저장 공간이다.
  - `performers`, `shows`, `venues`로 나뉘어 있다.
  - 관계:
    - 도메인 데이터 자체는 DB/H2/Oracle에 있고, 이 폴더는 파일 자산을 별도로 담는 위치다.
    - API가 직접 이 경로를 참조하는지, 외부 URL을 쓰는지는 시드 데이터와 정적 리소스 설정을 같이 봐야 한다.

- `storage/build/`
  - Gradle 산출물이다.

### `support/`

여러 모듈이 공유하는 보조 리소스를 둔다.

- `support/logging`
  - 공통 로깅 설정 리소스 모듈이다.
  - 관계:
    - `core/core-api`가 의존하여 실행 시 로깅 설정을 가져간다.

- `support/build/`
  - Gradle 산출물이다.

### `docs/`

사람이 읽는 설계/작업 기록을 둔다.

- `docs/superpowers/specs`
  - 설계 문서 저장 위치다.
  - 큰 구조 변경이나 설계 비중이 큰 작업은 여기에 설계 문서를 남긴다.

- `docs/superpowers/plans`
  - 구현 계획 문서 저장 위치다.
  - 보통 `specs`의 설계 문서와 같은 주제로 짝을 이룬다.

- `docs/sql`
  - SQL 메모, 스키마 검토, 쿼리 기록 성격의 문서를 둔다.

관계:

- 구조 변경 작업은 `specs`와 `plans`를 함께 남기는 현재 흐름을 따른다.
- 코드만 보고 의도를 판단하기 어려우면 먼저 `docs/superpowers`에서 같은 주제의 문서를 찾는다.

### `.codex/`

Codex 전용 실행 설정 폴더다.

- `.codex/config.toml`
  - Codex 런타임 설정, MCP 서버, 멀티 에이전트 활성화 여부를 정의한다.
  - `persistent_instructions`로 루트 `AGENTS.md`를 항상 따르도록 연결되어 있다.

- `.codex/agents/*.toml`
  - 역할별 에이전트 설정이다.
  - 예: `planner.toml`, `code-reviewer.toml`, `java-reviewer.toml`

관계:

- 복잡한 작업에서 어떤 에이전트를 쓸지 결정할 때 이 폴더 정의를 기준으로 본다.
- 루트 `AGENTS.md`는 사람/에이전트 공통 지침이고, `.codex/agents`는 역할별 상세 행동 규칙이다.

### `.agents/`

프로젝트 로컬 스킬과 플러그인 메타데이터를 둔다.

- `.agents/skills`
  - 이 저장소에서 직접 참조하는 스킬 정의들이다.
- `.agents/plugins/marketplace.json`
  - 로컬 플러그인 메타데이터다.

관계:

- 이 저장소 작업에서 프로젝트 스킬 경로는 루트 `skills/`가 아니라 `.agents/skills/`를 우선 본다.
- Codex가 프로젝트 로컬 스킬/플러그인을 해석할 때 이 폴더가 기준점이다.

### `.github/`

GitHub 협업 자동화 설정이다.

- `.github/workflows/deploy.yml`
  - 현재 배포 흐름의 기준이다.
  - 관계:
    - `./gradlew clean :core:core-api:bootJar -x test` 기준과 맞물린다.

- `.github/ISSUE_TEMPLATE`
  - 이슈 템플릿 보관 위치다.

### `gradle/`

Gradle wrapper 파일을 둔다.

- `gradle/wrapper`
  - 로컬/CI에서 동일한 Gradle 실행기를 사용하게 한다.

관계:

- 루트 `gradlew`, `gradlew.bat`와 함께 사용된다.

### 생성물 또는 로컬 실행용 폴더

아래 폴더는 소스보다 실행 산출물 또는 로컬 상태에 가깝다.

- `build/`
- `.gradle/`
- `.gradle-user-home/`
- `.gradle-user-home-hold-fix/`
- `.tmp/`
- `mysql-data/`
- `.idea/`
- `.backup/`

원칙:

- 특별히 요청받지 않으면 수정하지 않는다.
- 문제 재현이나 로컬 환경 확인에는 참고할 수 있지만, 기능 구현의 근거 소스로 삼지 않는다.

## 파일 간 관계를 읽는 핵심 흐름

이 저장소는 대체로 아래 흐름으로 연결된다.

```text
HTTP/WebSocket 요청
  -> core/core-api 의 controller, config/security
  -> core/core-domain 의 command/query/service
  -> repository(JPA/RDB) + port
  -> core/core-infra 의 Redis/WebSocket/AOP 구현
  -> 필요 시 support/event/publisher
  -> core/core-api 응답 DTO 또는 WebSocket 메시지
```

대표적인 연결 예시는 아래와 같다.

- 인증
  - `core/core-api/.../AuthController.java`
  - `core/core-api/.../config/security/*`
  - `core/core-domain/.../auth/*`
  - 관계: Controller가 인증 요청을 받고, security 설정이 토큰/Principal을 해석하며, 도메인 auth 패키지가 회원/토큰/OAuth2 흐름을 처리한다.

- 좌석 선택/홀드
  - `core/core-api/.../SeatSelectionController.java`, `HoldController.java`
  - `core/core-domain/.../performanceseat/*`
  - `core/core-domain/.../hold/*`
  - `core/core-infra/.../redis/*`
  - 관계: API 요청이 좌석 상태 변경을 시작하고, Redis store와 expiration handler가 TTL 기반 상태를 관리하며, 이벤트 발행기가 실시간 상태 전파를 담당한다.

- 주문
  - `core/core-api/.../OrderController.java`
  - `core/core-domain/.../order/*`
  - `core/core-domain/.../hold/*`
  - 관계: 주문은 hold 수명주기와 직접 연결되며, 만료 listener/scheduler가 주문 종료 보정을 담당한다.

## 패키지 경계에서 특히 주의할 점

- `core/core-api`는 진입점과 설정에 집중한다.
  - 비즈니스 규칙이나 저장소 접근 로직을 넣지 않는다.
- `core/core-domain`은 기능 중심 패키지다.
  - 새 코드를 추가할 때도 기술 레이어보다 도메인 단위로 먼저 위치를 찾는다.
- `infra`는 기술 의존을 가두는 위치다.
  - Redis, Redisson, WebSocket, HTTP client 같은 세부 구현은 가능한 한 이쪽으로 모은다.
- 구조 규칙은 테스트가 강제한다.
  - `core/core-domain/src/test/java/com/ticket/core/domain/CoreDomainArchitectureTest.java`
  - `core/core-domain/src/test/java/com/ticket/core/domain/CoreDomainModuleStructureTest.java`

## 자주 쓰는 검증/실행 명령

```bash
docker compose up -d redis
./gradlew :core:core-api:bootRun
./gradlew :core:core-api:compileJava
./gradlew :core:core-domain:test
./gradlew clean :core:core-api:bootJar -x test
```

설명:

- `bootRun`: 로컬 API 실행
- `compileJava`: 빠른 컴파일 검증
- `:core:core-domain:test`: 도메인 로직과 ArchUnit 구조 규칙 검증
- `bootJar`: 현재 배포 workflow와 맞물리는 산출물 생성

## 작업 시 빠른 판단 기준

- API 입력/응답을 바꾸면 먼저 `core/core-api`를 본다.
- 도메인 규칙, 주문/홀드/좌석/대기열 로직을 바꾸면 `core/core-domain`를 본다.
- Redis 키, TTL, 만료 이벤트를 바꾸면 `storage/redis-core`보다 `core/core-domain`의 `store`/`port`, `core/core-infra`의 구현을 먼저 본다.
- 로깅 형식이나 공통 로그 설정은 `support/logging`을 본다.
- 큰 구조 변경은 `docs/superpowers/specs`와 `docs/superpowers/plans`를 함께 갱신할지 확인한다.
- 모듈 경계를 건드리면 `settings.gradle`, 각 모듈 `build.gradle`, ArchUnit 테스트를 같이 본다.
