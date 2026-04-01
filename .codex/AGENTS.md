# ECC for Codex CLI

이 문서는 루트 [AGENTS.md](/Users/mn040/IdeaProjects/ticket/AGENTS.md)를 보완하는 `.codex` 전용 운영 가이드다.
이 저장소의 기본 기준은 Java, Spring Boot, Gradle, JUnit, Redis, Querydsl이다. 다른 템플릿 기준 예시는 적용하지 않는다.

## 기본 원칙

- 실행 기준 문서는 [README_CODEX.md](/Users/mn040/IdeaProjects/ticket/README_CODEX.md) 와 [README_ARCHITECTURE.md](/Users/mn040/IdeaProjects/ticket/README_ARCHITECTURE.md) 다.
- 구조 변경은 현재 모듈 경계와 아키텍처 테스트를 존중한다.
- 범용 에이전트도 이 저장소에서는 Java/Spring/Gradle 워크플로를 우선 사용한다.
- 명령 예시는 가능하면 `./gradlew` 기준으로 작성한다.

## 저장소 기준 기술 스택

- Language: Java 25
- Framework: Spring Boot 3.2
- Build: Gradle Wrapper
- Persistence: Spring Data JPA, Querydsl
- Infra: Redis, Redisson
- Test: JUnit 5, Spring Boot Test, ArchUnit

## 권장 검증 명령

작업 성격에 맞춰 아래 명령을 우선 사용한다.

```bash
./gradlew :core:core-api:compileJava
./gradlew :core:core-domain:compileJava
./gradlew :core:core-domain:test
./gradlew :core:core-api:test
./gradlew clean :core:core-api:bootJar -x test
```

아키텍처나 패키지 경계를 건드리면 아래 검증을 우선 확인한다.

```bash
./gradlew :core:core-domain:test --tests "com.ticket.core.domain.CoreDomainArchitectureTest"
./gradlew :core:core-domain:test --tests "com.ticket.core.domain.CoreDomainModuleStructureTest"
```

## Skills Discovery

프로젝트 스킬은 `.agents/skills/` 기준으로 읽는다. 범용 스킬이라도 이 저장소에서는 Java/Spring/Gradle 문맥에 맞게 해석한다.

자주 쓰는 스킬:
- `coding-standards`
- `backend-patterns`
- `security-review`
- `tdd-workflow`
- `verification-loop`
- `api-design`

## Multi-Agent Support

Codex 다중 에이전트는 `.codex/config.toml` 의 `[features] multi_agent = true` 로 켜져 있다.

- 역할 정의: `.codex/agents/*.toml`
- 상태 확인/조정: `/agent`
- 역할 설명과 실제 저장소 기준이 충돌하면, 저장소 기준 문서가 우선한다.

현재 저장소에서 특히 유효한 역할:
- `java-reviewer`
- `java-build-resolver`
- `database-reviewer`
- `planner`
- `security-reviewer`
- `refactor-cleaner`

## 문서 작성 기준

- 문서와 기본 응답은 한국어를 우선 사용한다.
- 경로, 모듈, 명령은 실제 저장소 구조와 일치해야 한다.
- 존재하지 않는 타 언어 런타임이나 프런트엔드 전제는 넣지 않는다.

## 보안 및 운영

Codex에는 Claude Code 스타일 hooks가 없으므로, 아래를 수동으로 반드시 확인한다.

1. 입력 검증이 경계에서 수행되는지 확인
2. 비밀값이 코드에 하드코딩되지 않았는지 확인
3. 변경 후 `git diff` 와 관련 테스트 결과를 함께 확인
4. 인증/인가, 트랜잭션, 락, 만료 처리 변경은 별도 검토
5. 필요한 경우 Spring 설정과 Redis 설정이 환경별로 분리되어 있는지 확인
