# Ticket Backend Architecture

## 1. 프로젝트 개요

현재 프로젝트는 멀티모듈 Gradle 구조이지만, 실제 아키텍처 성격은 `core-api + core-business` 중심의 모듈러 모놀리스에 가깝다. `storage:redis-core`, `support:logging`은 얇은 지원 모듈이고, 실제 비즈니스 중심은 `core-api + core-business` 조합에 있다.

실제 포함 모듈은 다음 4개다.

- `core:core-api`
- `core:core-business`
- `storage:redis-core`
- `support:logging`

`core-enum` 모듈은 현재 존재하지 않는다.
Gradle 프로젝트 이름은 `core:core-business`지만, 현재 소스 디렉터리는 아직 `core/core-domain` 경로를 사용한다.

## 2. 모듈 책임과 의존 방향

### 2.1 `core:core-api`

실행 모듈이다. 웹 진입점과 보안, HTTP 설정, WebSocket 설정, 컨트롤러를 담당한다.

주요 책임:

- REST Controller
- 요청/응답 DTO
- Spring Security, JWT, OAuth2 설정
- WebSocket 진입 설정
- 공통 응답 포맷

의존:

- `core:core-business`
- `support:logging`

### 2.2 `core:core-business`

비즈니스 기능의 중심이 되는 business core 모듈이다. 이름만 보면 순수 도메인 계층처럼 보이지만, 현재 실제 책임은 순수 도메인 모델에 한정되지 않는다. application, domain, infra 성격의 코드가 함께 들어가 있으며, 점진적으로 `infra` 패키지로 기술 의존을 분리하는 중이다.

즉 현재 문맥에서 Gradle 모듈 `core:core-business`는 "순수 domain module"이 아니라 "비즈니스 규칙, 유스케이스, 저장소, 일부 기술 구현을 함께 담는 핵심 비즈니스 모듈"로 이해하는 것이 맞다.

주요 책임:

- 기능별 use case
- JPA entity / repository
- query repository
- 도메인 규칙
- Redis 기반 store
- WebSocket publish 보조
- 외부 HTTP interface client
- 분산락 AOP
- Redis expiration listener, Querydsl, P6Spy 같은 공통 인프라 설정

의존:

- `storage:redis-core`
- Spring Web / WebSocket / Data JPA / Querydsl / P6Spy
- Oracle / H2 드라이버와 annotation processor

### 2.3 `storage:redis-core`

Redis 관련 공통 의존성과 최소 부트스트랩 설정을 제공하는 얇은 shared leaf module이다. 이 모듈의 목적은 Redis/Redisson wiring과 공통 리소스를 한곳에 두고, 실제 비즈니스 Redis 사용 구현은 `core-domain`에 남기는 것이다.

주요 책임:

- `spring-boot-starter-data-redis`
- `redisson-spring-boot-starter`
- `redis.yml` 과 `RedissonConfig` 제공

실제 Redis 사용 구현체는 현재 `core-domain`의 각 기능별 `infra` 패키지에 위치한다. `core-api`는 [application.yml](c:/Users/mn040/IdeaProjects/ticket/core/core-api/src/main/resources/application.yml) 에서 `redis.yml` 을 import해 이 모듈의 리소스를 소비한다.

### 2.4 `support:logging`

로깅 관련 공통 설정 리소스를 제공하는 얇은 shared leaf module이다. 현재 Java 소스 없이 리소스만 담고 있으며, 목적은 실행 모듈에서 로깅 설정 파일을 직접 들고 있지 않도록 분리하는 것이다.

`core-api`는 [application.yml](c:/Users/mn040/IdeaProjects/ticket/core/core-api/src/main/resources/application.yml) 에서 `logging.yml` 을 import해 이 모듈의 리소스를 소비한다.

## 3. 현재 패키지 구조

### 3.1 `core-api`

- `com.ticket.core.api.controller`
  - HTTP 엔드포인트
- `com.ticket.core.api.controller.docs`
  - Swagger 문서 인터페이스
- `com.ticket.core.api.controller.request`
  - 요청 DTO
- `com.ticket.core.config`
  - 웹, JPA Auditing, HTTP service, WebSocket 설정
- `com.ticket.core.config.security`
  - JWT, OAuth2, 인증/인가 구성
- `com.ticket.core.support.response`
  - 공통 응답 래퍼

### 3.2 `core-domain`

기능별 패키지를 기본 축으로 잡고 있다. 다만 이 모듈은 "엔티티만 있는 도메인 계층"이 아니라 business core이므로, 기능 패키지 안에 유스케이스, 저장소, 기술 구현체가 함께 존재한다.

- `com.ticket.core.domain.auth`
- `com.ticket.core.domain.member`
- `com.ticket.core.domain.order`
- `com.ticket.core.domain.hold`
- `com.ticket.core.domain.queue`
- `com.ticket.core.domain.show`
- `com.ticket.core.domain.performanceseat`

기능 내부에서는 다음 하위 패키지 패턴을 사용한다.

- `command`
- `query`
- `model`
- `repository`
- `store`
- `support`
- `event`
- `infra`

현재는 기술 의존 코드가 `infra` 하위로 점진적으로 이동되어 있다. 완전히 분리된 `application/domain/infra` 3계층 모듈 구조라기보다, 하나의 business core 모듈 안에서 패키지 경계를 강화하는 단계에 가깝다.

예시:

- `com.ticket.core.infra.lock`
  - 분산락 어노테이션/AOP
- `com.ticket.core.infra.config`
  - Querydsl, P6Spy 설정
- `com.ticket.core.infra.redis`
  - Redis 만료 listener 설정
- `com.ticket.core.domain.auth.infra.oauth2`
  - Kakao HTTP interface, OAuth2 auth code 저장 서비스
- `com.ticket.core.domain.auth.infra.token`
  - Redis refresh token 저장 서비스
- `com.ticket.core.domain.hold.infra`
  - Redis hold 저장소
- `com.ticket.core.domain.queue.infra`
  - Redis queue ticket 저장소
- `com.ticket.core.domain.performanceseat.infra.store`
  - Redis seat selection 저장소
- `com.ticket.core.domain.performanceseat.infra.realtime`
  - WebSocket seat event publisher

## 4. 기능별 구조 원칙

### 4.1 Controller

Controller는 가능한 한 얇게 유지한다.

- 요청 검증
- 인증 principal 추출
- use case 호출
- 응답 포맷 반환

비즈니스 규칙이나 저장소 접근은 controller에 두지 않는다.

### 4.2 Command / Query

- `command`
  - 상태를 변경하는 유스케이스
- `query`
  - 조회 전용 유스케이스와 조회 저장소

최근 정리 기준으로 query repository는 더 이상 use case 내부 DTO에 직접 의존하지 않고, 기능별 query model view를 반환한다.

예시:

- `show.query.model.ShowListItemView`
- `show.query.model.ShowSearchItemView`
- `performanceseat.query.model.SeatInfoView`
- `performanceseat.query.model.SeatStateView`

use case는 이 view를 API 또는 application output으로 변환해 반환한다.

## 5. 저장소 구조

### 5.1 RDB

주 영속 저장소는 RDB다.

주요 대상:

- 회원
- 공연/회차
- 좌석
- 주문
- hold 이력

JPA entity와 Spring Data repository는 대부분 `core-domain`에 존재한다.

### 5.2 Redis

Redis는 짧은 수명 상태와 동시성 제어, 토큰 저장, 실시간 좌석/대기열 처리에 사용한다.

주요 대상:

- seat selection
- seat hold
- queue ticket
- refresh token
- OAuth2 one-time auth code

Redis 구현체는 기능별 `infra` 패키지에 위치한다.

## 6. 실시간 처리 구조

### 6.1 좌석 선택 / 홀드

- 좌석 선택과 홀드는 Redis TTL을 사용한다.
- 만료 시 listener 및 보정용 스케줄러로 후속 정리를 수행한다.
- 좌석 상태 변경은 WebSocket 메시지로 전파한다.

### 6.2 주문 종료 보정

- 즉시 이벤트 처리만으로 끝내지 않고, 보정용 스케줄러를 함께 둔다.
- 목적은 listener 누락이나 운영 중 일시 장애가 있어도 정합성을 다시 맞추는 것이다.

## 7. 동시성 제어

분산락은 `@DistributedLock` + AOP로 처리한다.

현재 구현 위치:

- 어노테이션: `com.ticket.core.infra.lock.DistributedLock`
- 실행부: `com.ticket.core.infra.lock.DistributedLockAop`

적용 예:

- 동일 회원/공연 조합의 중복 주문 시작 방지
- 동일 좌석 동시 점유 방지
- 대기열 입장/퇴장 경쟁 상태 제어

## 8. 아키텍처 규칙

`core-domain`에는 ArchUnit 기반 구조 테스트가 있다.

현재 강제하는 핵심 규칙:

- `infra` 바깥에서는 `org.redisson..`에 직접 의존하지 않는다.
- `infra` 바깥에서는 `org.springframework.messaging..`에 직접 의존하지 않는다.
- `infra` 바깥과 `core.config` 바깥에서는 HTTP interface client annotation에 직접 의존하지 않는다.

목적은 기술 의존이 도메인/use case 전반으로 번지는 것을 CI에서 조기에 막는 것이다.

## 9. 현재 구조의 특징

장점:

- 기능별 패키지 분리가 비교적 명확하다.
- controller가 얇고 use case 중심 흐름이 유지된다.
- Redis TTL, listener, scheduler 조합으로 만료 복구 전략이 있다.
- query repository와 use case output 결합을 분리하기 시작했다.

한계:

- 여전히 `core-domain` 하나에 많은 역할이 모여 있다.
- JPA, Redis, WebSocket, 외부 HTTP client가 같은 모듈에 공존한다.
- 모듈 이름만 보면 순수 도메인 계층으로 오해하기 쉽다.
- 완전한 모듈 분리보다 패키지 경계 강화 단계에 가깝다.

## 10. 다음 정리 방향

현재 추천 방향은 빅뱅 리팩터링이 아니라 점진적 정리다. 당장은 이름을 바꾸거나 모듈을 크게 쪼개기보다, 문서와 테스트에서 `core-domain`을 business core로 명확히 정의하고 경계를 강화하는 편이 맞다.

1. 기능별 기술 구현을 `infra` 패키지로 계속 이동한다.
2. query 전용 view/model과 application output을 분리 유지한다.
3. ArchUnit 규칙을 추가해 의존 방향을 더 구체적으로 고정한다.
4. 필요 시 이후에 Gradle 모듈 단위로 `application / domain / infra`를 분리한다.
