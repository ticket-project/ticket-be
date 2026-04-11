이 PR의 변경사항을 리뷰하세요.

## 리뷰 규칙

1. 모든 코멘트는 한국어로 작성하세요.
2. 모든 이슈 앞에 심각도 태그를 붙이세요:
   - [P0-Bug]: 버그, 데이터 손실, 보안 → 반드시 수정
   - [P0-Arch]: 아키텍처 위반 (모듈 경계, 인프라 격리) → 반드시 수정
   - [P1-Concurrency]: 동시성, 분산 락, TTL 정합성 → 수정 권장
   - [P1-Test]: 테스트 누락/불일치 → 수정 권장
   - [P2-Suggestion]: 개선 제안 → 판단 필요
3. P2-Style (순수 스타일) 코멘트는 남기지 마세요.
4. P0/P1 위주로 집중하세요.

## 참조 문서

아키텍처 규칙, 설계 원칙, 동시성 규칙은 다음 파일에 정의되어 있습니다:
- AGENTS.md — Codex 에이전트 지침
- CLAUDE.md — 프로젝트 맵
- docs/ARCHITECTURE.md — 모듈 의존 방향, 인프라 격리
- docs/RELIABILITY.md — 분산 락, TTL, 트랜잭션 규칙
- docs/design-docs/core-beliefs.md — 설계 원칙

## 리뷰 절차

1. `git diff $BASE_REF..HEAD`로 변경사항을 확인하세요.
2. 변경된 파일 각각에 대해 AGENTS.md의 심각도 기준으로 이슈를 찾으세요.
3. 이슈가 없으면 "리뷰 통과 — P0/P1 이슈 없음"을 출력하세요.
4. 이슈가 있으면 심각도 태그 + 파일 경로 + 설명 + 수정 방향을 포함하세요.

## 출력 형식

```
## Codex 코드 리뷰

### 요약
- P0: N건, P1: N건, P2: N건

### 이슈 목록

#### [P0-Arch] core-domain에서 Redisson 직접 참조
- 파일: core/core-domain/src/.../SomeService.java:42
- 설명: infra 패키지 밖에서 Redisson 클래스를 직접 import하고 있습니다.
- 수정: ..infra.. 패키지로 이동하세요.
- 참고: docs/ARCHITECTURE.md#인프라-격리-규칙
```
