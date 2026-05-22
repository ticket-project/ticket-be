# Ticket

공연/전시 티켓팅 백엔드 프로젝트다. 순간 트래픽, 대기열, 좌석 선택/선점, 주문 흐름을 Spring Boot 멀티 모듈 구조에서 검증한다.

## 문서 읽는 순서

현재 개발과 리뷰 기준은 아래 순서로 본다.

1. [README_CODEX.md](README_CODEX.md)
   - 현재 코드 기준 개발자 온보딩 문서
   - API, 도메인 흐름, 로컬 실행, 미구현 범위 확인
2. [README_ARCHITECTURE.md](README_ARCHITECTURE.md)
   - Gradle 모듈, 패키지 경계, 구조 테스트 기준 확인
3. [README_PRODUCT.md](README_PRODUCT.md)
   - 제품 관점의 사용자 흐름과 구현 범위 확인
4. [AGENTS.md](AGENTS.md)
   - Codex/에이전트 작업 기준과 문서 우선순위 확인
5. [README_JUNIE.md](README_JUNIE.md)
   - 과거 AI 초안 문서
   - 현재 개발 기준 문서가 아니며, 요구사항 아이디어 참고용으로만 본다.

## 로컬 실행

```bash
docker compose up -d redis
./gradlew :core:core-api:bootRun
```

빠른 검증은 아래 명령을 우선 사용한다.

```bash
./gradlew :core:core-api:compileJava
./gradlew :core:core-domain:test
./gradlew clean :core:core-api:bootJar -x test
```

Windows PowerShell에서는 `gradlew.bat`를 사용한다.

```powershell
.\gradlew.bat :core:core-api:compileJava
.\gradlew.bat :core:core-domain:test
```

## 주요 폴더

- `core/core-api`
  - Spring Boot 실행 모듈
  - HTTP/WebSocket 진입점, 보안 설정, API 응답 DTO를 둔다.
- `core/core-domain`
  - 비즈니스 규칙 중심 모듈
  - auth, hold, order, queue, performanceseat, show 등 도메인 흐름을 둔다.
- `core/core-infra`
  - Redis, Redisson, 외부 HTTP, WebSocket publisher, AOP 같은 기술 구현을 둔다.
- `storage/redis-core`
  - Redis 관련 공통 의존성 모듈
- `support/logging`
  - 공통 로깅 설정 모듈
- `docs`
  - 설계 문서, 구현 계획, SQL 메모, 부하 테스트 문서
- `load-tests`
  - 로컬 부하 테스트 자산

## AI와 에이전트 설정

AI 관련 설정은 자동화 동작과 연결될 수 있으므로 임의로 이동하거나 삭제하지 않는다.

- [AGENTS.md](AGENTS.md)
  - 저장소 공통 작업 기준
- [.codex/AGENTS.md](.codex/AGENTS.md)
  - Codex 리뷰 보조 지침
- [.codex/config.toml](.codex/config.toml)
  - Codex 런타임, MCP, 멀티 에이전트 설정
- `.codex/agents/*.toml`
  - Codex 역할별 에이전트 정의
- `.agents/skills`
  - 프로젝트 로컬 스킬
- [.agents/plugins/marketplace.json](.agents/plugins/marketplace.json)
  - 로컬 플러그인 메타데이터
- [.github/copilot-instructions.md](.github/copilot-instructions.md)
  - GitHub Copilot용 저장소 지침
- `.github/instructions/*.md`
  - GitHub AI 리뷰용 보조 지침
- `.github/workflows/claude*.yml`
  - Claude GitHub Action 워크플로
- [.pr_agent.toml](.pr_agent.toml)
  - PR-Agent/Qodo 계열 리뷰 설정
- [.coderabbit.yaml](.coderabbit.yaml)
  - CodeRabbit 리뷰 설정

## 초안과 보관 후보

[README_JUNIE.md](README_JUNIE.md)는 과거 AI 작성 초안이다. 현재 코드와 다른 내용이 섞여 있을 수 있으므로 개발 기준으로 사용하지 않는다.

후속 정리에서 아래를 별도로 결정한다.

- `README_JUNIE.md`를 `docs/archive/`로 이동할지 여부
- `.codex/agents` 중 현재 저장소와 맞지 않는 범용 에이전트 유지 여부
- `.agents/skills` 하위 외부 범용 스킬 유지 여부
- Copilot, Claude, CodeRabbit, PR-Agent 지침 중복 축소 여부
