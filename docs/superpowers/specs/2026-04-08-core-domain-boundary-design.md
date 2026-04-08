# core-domain 모듈 책임 경계 정의

## 목적

이 문서는 `core-domain` 모듈이 담당하는 책임과 허용 의존성 경계를 명시한다.
아키텍처 규칙 위반을 방지하고, 코드 리뷰 및 신규 코드 추가 시 기준으로 활용한다.

## 모듈 책임

`core-domain`은 **비즈니스 규칙**을 중심으로 한다.

| 허용 책임 | 설명 |
|-----------|------|
| 도메인 엔티티 / 값 객체 | JPA 엔티티, 도메인 모델, 열거형 |
| 유스케이스 (command / query) | 비즈니스 흐름 조정, 도메인 규칙 적용 |
| 도메인 이벤트 | 상태 전이를 나타내는 이벤트 객체 |
| 저장소 인터페이스 (repository / store) | 영속 저장소 추상화 |
| 인프라 어댑터 (`..infra..`) | Redis, WebSocket, 외부 API, 스케줄러 등 기술 구현 |
| 공유 지원 (`com.ticket.core.support`) | 예외, 커서, 락 어노테이션 등 크로스커팅 유틸리티 |

## 허용 의존성

```
core-domain
  ├── spring-boot-starter-data-jpa   (영속성)
  ├── spring-boot-starter-web        (REST 예외 처리 지원)
  ├── spring-boot-starter-websocket  (WebSocket 메시징 - infra 패키지에서만)
  ├── storage:redis-core             (Redis / Redisson - infra 패키지에서만)
  └── 공통 라이브러리 (Lombok, QueryDSL, Jackson 등)
```

## 패키지 경계 규칙

### 1. infra 패키지 외부에서 직접 참조할 수 없는 기술

| 기술 / 패키지 | 허용 범위 |
|--------------|-----------|
| `org.redisson..*` | `..infra..` 패키지 내부에서만 |
| `org.springframework.data.redis..*` | `..infra..` 패키지 내부에서만 |
| `org.springframework.messaging..*` (WebSocket) | `..infra..` 패키지 내부에서만 |
| `org.springframework.scheduling.annotation..*` (`@Scheduled`) | `..infra..` 패키지 내부에서만 |
| `org.springframework.web.service.annotation..*` | `..infra..` 및 `config` 패키지 내부에서만 |

### 2. domain 패키지는 core.infra 패키지에 직접 의존하지 않는다

`com.ticket.core.domain..*` 클래스는 `com.ticket.core.infra..*` 클래스를 직접 임포트해서는 안 된다.
분산 락(`@DistributedLock`)과 같이 도메인 클래스에서 사용하는 크로스커팅 어노테이션은
`com.ticket.core.support.lock` 패키지에 위치한다.

### 3. 스케줄러는 infra 패키지에 위치한다

`@Scheduled` 어노테이션을 사용하는 클래스는 반드시 `..infra..` 패키지 하위에 위치해야 한다.
스케줄러는 프레임워크가 실행을 주도하는 기술 구현이며, 도메인 command 패키지에 두지 않는다.

현재 위치:
- `com.ticket.core.domain.order.infra.scheduler.OrderExpirationScheduler`
- `com.ticket.core.domain.order.infra.scheduler.HoldReleaseOutboxScheduler`

## 분산 락 어노테이션 (`@DistributedLock`)

`@DistributedLock`은 AOP 기반의 크로스커팅 어노테이션으로, `@Transactional`과 유사하게
도메인 클래스에서 직접 사용할 수 있다.

- **어노테이션 위치**: `com.ticket.core.support.lock.DistributedLock` (인프라 로직 없음)
- **AOP 구현 위치**: `com.ticket.core.infra.lock.DistributedLockAop` (Redisson 기반)

도메인 클래스가 `@DistributedLock`을 사용하더라도 infra 패키지에 의존하지 않으며,
단위 테스트에서 AOP는 동작하지 않으므로 인프라 어댑터 없이 테스트 가능하다.

## 유스케이스 테스트 가능성

핵심 유스케이스(`CreateOrderUseCase`, `CancelOrderUseCase`, `ExpireOrderUseCase` 등)는
다음 이유로 인프라 어댑터 없이 단위 테스트가 가능하다:

1. **`@DistributedLock`**: AOP 어노테이션이므로 단위 테스트 시 적용되지 않는다.
2. **`ApplicationEventPublisher`**: Spring 인터페이스로 Mockito로 모킹 가능하다.
3. **Repository / Store**: 인터페이스 기반이므로 모킹 가능하다.

## ArchUnit 검증

`CoreDomainArchitectureTest`에서 아래 규칙을 런타임 회귀 방지용으로 검증한다:

- `infra` 밖에서는 Redisson 직접 의존 금지
- `infra` 밖에서는 Spring Data Redis 직접 의존 금지
- `infra` 밖에서는 WebSocket 메시징 직접 의존 금지
- `infra` 밖에서는 `@Scheduled` 사용 금지
- `domain` 클래스에서 `com.ticket.core.infra..*` 직접 의존 금지
