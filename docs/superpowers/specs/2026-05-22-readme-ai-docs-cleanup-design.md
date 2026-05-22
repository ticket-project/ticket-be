# README and AI Docs Cleanup Design

## 목표

루트 README 계열 문서와 AI 관련 설정 문서의 역할을 분리해, 저장소 진입자가 어떤 문서를 먼저 봐야 하는지 명확히 한다.

- 루트 README는 프로젝트 진입점과 문서 지도로 정리한다.
- 최신 개발 기준은 `README_CODEX.md`와 `README_ARCHITECTURE.md`에 둔다.
- 오래된 초안 문서는 즉시 삭제하지 않고 초안 또는 보관 후보임을 명확히 표시한다.
- `.codex`, `.agents`, `.github` 아래 AI 자동화 설정은 1차 작업에서 이동하거나 삭제하지 않는다.

## 확인한 현재 상태

- `README_CODEX.md`는 현재 코드 기준 개발자 온보딩과 리뷰 기준을 담고 있다.
- `README_ARCHITECTURE.md`는 멀티 모듈 구조와 모듈 경계 기준을 담고 있다.
- `README_PRODUCT.md`는 제품 관점의 사용자 흐름과 구현 범위를 요약한다.
- `README.md`는 초기 요구사항과 TDD 체크리스트 성격이 강해 루트 진입 문서로는 현재성이 낮다.
- `README_JUNIE.md`는 AI가 작성한 과거 요구사항 초안과 상세 초안이 섞여 있다.
- `AGENTS.md`는 에이전트 공통 작업 기준이며, `.codex/AGENTS.md`는 Codex 리뷰 보조 지침이다.
- AI 관련 설정은 `.codex`, `.agents`, `.github/copilot-instructions.md`, `.github/workflows/claude*.yml`, `.pr_agent.toml`, `.coderabbit.yaml`에 분산되어 있다.

## 비범위

- Java/Spring 코드 변경
- Gradle 모듈 구조 변경
- AI 자동화 파일의 삭제, 이동, 비활성화
- `.agents/skills` 하위 스킬 내용 정리
- `.github/workflows` 실행 조건 변경
- 기존 대기열 관련 미커밋 변경 수정

## 접근 방법

### 1. README 계열만 먼저 정리

가장 안전한 방식이다. 루트 README를 문서 지도로 바꾸고, 기준 문서와 초안 문서의 역할을 표시한다. 자동화 설정을 건드리지 않으므로 CI와 AI 리뷰 동작 영향이 작다.

### 2. AI 설정까지 함께 구조 정리

`.codex`, `.agents`, GitHub AI 리뷰 설정을 함께 재배치하거나 축소한다. 중복은 크게 줄일 수 있으나, Codex, Claude, Copilot, CodeRabbit, Qodo 계열 자동화의 호환성 검증이 필요하다.

### 3. 문서와 AI 설정을 한 번에 통합

가장 깔끔한 최종 상태를 만들 수 있지만 변경 범위가 크다. 현재 작업트리에 다른 코드 변경이 존재하므로 이번 단계에서는 적합하지 않다.

선택한 방식은 1번이다. 1차 정리로 README 계열의 역할과 링크를 고정한 뒤, AI 설정 정리는 별도 후속 작업으로 분리한다.

## 설계

### 1. 루트 README 역할

`README.md`는 긴 요구사항 초안 대신 저장소 진입점으로 정리한다.

포함할 내용은 아래로 제한한다.

- 프로젝트 한 줄 설명
- 현재 기준 문서 읽는 순서
- 주요 실행 명령
- 주요 폴더 역할
- AI/에이전트 설정 위치 요약
- 오래된 초안 문서 안내

상세 구현 설명은 `README_CODEX.md`, 구조 설명은 `README_ARCHITECTURE.md`, 제품 설명은 `README_PRODUCT.md`로 연결한다.

### 2. 기준 문서 역할

`README_CODEX.md`는 최신 코드 기준 개발 문서로 유지한다.

- API와 도메인 흐름
- 현재 구현 범위
- 로컬 실행 방법
- 운영 반영 시 주의점
- 미구현 영역

`README_ARCHITECTURE.md`는 구조 기준 문서로 유지한다.

- Gradle 모듈 책임
- 모듈 간 의존 방향
- 패키지 경계
- 구조 테스트 기준
- 다음 구조 정리 방향

`README_PRODUCT.md`는 제품 관점 문서로 유지한다.

- 사용자 흐름
- 구현 완료와 계획 중 범위
- 서비스 차별점
- 제품 제약

### 3. 초안 문서 처리

`README_JUNIE.md`는 바로 삭제하지 않는다. 문서 상단에 아래 성격을 명시하는 방향으로 정리한다.

- AI 작성 초안
- 현재 코드와 차이가 있을 수 있음
- 개발 기준 문서가 아님
- 후속 작업에서 `docs/archive/` 이동 또는 삭제 검토

`README.md`에 있던 과거 요구사항 또는 TDD 메모 중 현재 코드 기준과 충돌할 수 있는 내용은 루트 README 본문에서 제거하거나, `README_JUNIE.md`와 같은 초안 문서로 참조만 남긴다.

### 4. AI 관련 설정 안내

1차 작업에서는 AI 관련 파일을 이동하지 않고, 루트 README 또는 `AGENTS.md`의 기존 설명과 맞춰 위치만 안내한다.

- `.codex/config.toml`: Codex 런타임, MCP, 멀티 에이전트 설정
- `.codex/agents/*.toml`: Codex 역할별 에이전트 정의
- `.codex/AGENTS.md`: Codex 리뷰 보조 지침
- `.agents/skills`: 프로젝트 로컬 스킬
- `.agents/plugins/marketplace.json`: 로컬 플러그인 메타데이터
- `.github/copilot-instructions.md`: Copilot용 저장소 지침
- `.github/workflows/claude*.yml`: Claude GitHub Action
- `.pr_agent.toml`: PR-Agent/Qodo 계열 리뷰 설정
- `.coderabbit.yaml`: CodeRabbit 리뷰 설정

자동화 동작이 걸린 파일은 문서 안내만 추가하고 내용 정리는 후속 작업으로 둔다.

## 검증 전략

문서 변경이므로 Java 빌드 또는 도메인 테스트는 필수 검증 대상이 아니다.

대신 아래 정적 검증을 수행한다.

- 변경 문서가 UTF-8 BOM 없이 저장되었는지 확인한다.
- README 계열 문서 링크가 존재하는 파일을 가리키는지 확인한다.
- `README.md`, `README_CODEX.md`, `README_ARCHITECTURE.md`, `README_PRODUCT.md`, `README_JUNIE.md`, `AGENTS.md`의 문서 우선순위 설명이 서로 충돌하지 않는지 확인한다.
- AI 설정 파일은 이동/삭제되지 않았는지 `git status`로 확인한다.

## 성공 기준

- 루트 README만 보고도 기준 문서와 보조 문서를 구분할 수 있다.
- 최신 개발 기준이 `README_CODEX.md`와 `README_ARCHITECTURE.md`임이 명확하다.
- `README_JUNIE.md`가 현재 기준 문서로 오해되지 않는다.
- AI 관련 설정의 위치와 역할을 찾을 수 있다.
- 자동화 파일 이동이나 삭제 없이 문서 정리만 완료된다.

## 후속 작업 후보

- `.codex/agents` 중 현재 저장소와 맞지 않는 범용 에이전트 정리
- `.agents/skills`에 포함된 외부 범용 스킬 유지 필요성 검토
- `.github`의 Copilot, Claude, CodeRabbit, PR-Agent 지침 중 중복 문구 축소
- `README_JUNIE.md`를 `docs/archive/`로 이동할지 결정
