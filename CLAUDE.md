# Ticket — AI Agent Guide

## 프로젝트 한 줄 요약

실시간 좌석 선택, 대기열, 분산 락을 활용하는 티켓 예매 시스템 (Spring Boot 4 + Redis + Oracle)

## 모듈 구조

```
ticket/
├── core/
│   ├── core-api        ← HTTP/WebSocket 진입점, 보안, 설정, Response DTO
│   └── core-domain     ← 비즈니스 로직, 도메인 모델, 이벤트, 인프라 추상화
├── storage/
│   └── redis-core      ← Redisson/Redis 연결 설정
└── support/
    └── logging         ← 로깅 YAML 설정
```

## 계층 규칙 (반드시 준수)

모듈 의존 방향 (단방향, 역방향 금지):
```
core-api (HTTP 진입점) ──▶ core-domain (비즈니스 로직) ──▶ redis-core (인프라)
```

도메인 패키지 내부 구조 (`domain/{도메인명}/`):
```
model/    ← 엔티티, 값 객체, 열거형 (의존 없음)
command/  ← 쓰기 유스케이스
query/    ← 읽기 유스케이스
event/    ← 도메인 이벤트, 리스너
infra/    ← 외부 시스템 연동 (Redis, 메시징, HTTP 클라이언트)
store/    ← 저장소 구현
```

교차 관심사(인증, 텔레메트리)는 infra 패키지를 통해서만 유입한다.

**core-api와 core-domain 경계:**
- core-domain은 순수 비즈니스 로직만 포함한다
- JWT, Swagger, Response DTO, CookieUtils는 core-api에만 존재한다
- Redisson, Spring Data Redis, Spring Messaging은 `..infra..` 패키지에서만 사용한다
- 위반 시 ArchUnit 테스트가 실패한다 → `CoreDomainArchitectureTest`, `CoreDomainModuleStructureTest` 참고

## 빌드 & 검증

```bash
# 빠른 컴파일 체크 (~10초)
./gradlew :core:core-api:compileJava

# 아키텍처 + 도메인 테스트 (~30초)
./gradlew :core:core-domain:test

# 전체 빌드 (배포용)
./gradlew clean :core:core-api:bootJar -x test
```

## 비즈니스 도메인 맵

상세: [docs/PRODUCT_SENSE.md](docs/PRODUCT_SENSE.md)

```
[사용자] → 대기열 진입(queue) → 좌석 선택(performanceseat)
        → 주문 생성(order) → 홀드 할당(hold) → 결제 확인
```

도메인 패키지 (`com.ticket.core.domain`):
| 패키지 | 역할 |
|---|---|
| `queue` | 대기열 진입/퇴장, 입장 토큰 TTL 관리 |
| `hold` | 좌석 임시 점유 (Redis TTL), 분산 락 |
| `order` | 주문 생성/만료/취소, Outbox 패턴 |
| `performanceseat` | 실시간 좌석 상태, Redis 선택/해제, WebSocket 브로드캐스트 |
| `performance` | 공연 일정, 예매 시간 |
| `show` | 공연 정보, 장르, 공연장 |
| `seat` | 좌석 물리 정보 (구역, 열, 번호, 좌표) |
| `member` | 회원 계정, OAuth2 |
| `auth` | 토큰 발급/갱신, OAuth2 제공자 |
| `showlike` | 공연 찜/좋아요 |
| `commoncode` | 공통 코드 (카테고리, 장르 등 참조 데이터) |

## 동시성 제어 규칙

상세: [docs/RELIABILITY.md](docs/RELIABILITY.md)

1. **분산 락**: 홀드 생성(좌석별 15초), 대기열(공연별 5초)
2. **Redis TTL**: 좌석 선택(5분), 홀드(공연 설정값), 대기열 토큰(QueueLevel별)
3. **트랜잭션 이벤트**: `@TransactionalEventListener(AFTER_COMMIT)` — 커밋 후 Redis/WebSocket 처리
4. **Outbox 패턴**: 홀드 해제는 `HoldReleaseOutbox`로 최종 일관성 보장

## 아키텍처 상세

- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) — 패키지 레이어링, 의존 방향
- [docs/design-docs/core-beliefs.md](docs/design-docs/core-beliefs.md) — 설계 원칙
- [docs/QUALITY_SCORE.md](docs/QUALITY_SCORE.md) — 도메인별 품질 등급

## 검증 스크립트

```bash
# 전체 검증 (컴파일 → 테스트 → 앱 부팅 → 스모크 테스트 → 정리)
./scripts/verify-fix.sh

# 스크린샷 포함 검증
CAPTURE_SCREENSHOTS=true ./scripts/verify-fix.sh

# 개별 실행
./scripts/app-start.sh          # 앱 부팅 (Redis + Spring Boot local)
./scripts/smoke-test.sh          # API 스모크 테스트
./scripts/app-stop.sh            # 앱 + Redis 정리

# PR 생성 + 리뷰
./scripts/pr-create.sh "제목" "요약"
./scripts/review-respond.sh                    # 현재 상태만 확인
./scripts/review-respond.sh --wait             # 리뷰 봇 응답 대기 후 확인 (최대 10분)
./scripts/review-respond.sh --wait --auto-merge # 대기 후 P0 없으면 자동 병합
./scripts/review-respond.sh --auto-merge 42    # 특정 PR 자동 병합

# 문서 정합성 검사
./scripts/doc-gardening.sh               # 검사만
./scripts/doc-gardening.sh --fix         # 검사 + 수정 지시 출력

# E2E + 스크린샷 (Playwright)
cd scripts/e2e && npm install && npm run install-browsers
node scripts/e2e/capture.js      # Swagger UI + API 스크린샷
node scripts/e2e/e2e-api-flow.js # 전체 API 흐름 테스트
```

## 에이전트 작업 스킬

### bug-fix (전체 자율 루프)
1. 버그 재현 테스트 작성 (실패 확인)
2. 최소 범위 수정
3. `./gradlew :core:core-domain:test` 통과 확인
4. `./scripts/verify-fix.sh` 실행 (앱 부팅 + 스모크 테스트)
5. `./scripts/pr-create.sh` 로 PR 생성
6. `./scripts/review-respond.sh --wait --auto-merge` 로 리뷰 대기 + 자동 병합
7. 종료 코드에 따라 분기:
   - `0` (병합 완료/가능) → 완료 보고
   - `2` (P0 이슈 / CI 실패) → 코멘트 읽고 수정 → 재푸시 → 6으로
   - `3` (대기 중) → 잠시 후 재확인

### new-feature
1. `docs/exec-plans/active/` 에 실행 계획 작성
2. 구현 (테스트 포함)
3. `./scripts/verify-fix.sh` 실행
4. 관련 문서 업데이트
5. `./scripts/pr-create.sh` 로 PR 생성
6. `./scripts/review-respond.sh --wait --auto-merge` 로 리뷰 대기 + 자동 병합
7. 완료 후 실행 계획을 `completed/`로 이동

### refactor
1. 영향 범위 분석 (의존 패키지, 테스트)
2. 기존 테스트 전부 통과 확인 (before)
3. 리팩터링 수행
4. `./scripts/verify-fix.sh` 실행
5. `./scripts/pr-create.sh` 로 PR 생성
6. `./scripts/review-respond.sh --wait --auto-merge` 로 리뷰 대기 + 자동 병합

### doc-gardening (정기 실행)
1. `./scripts/doc-gardening.sh` 로 문서 정합성 검사
2. 이슈가 있으면 수정
3. `./scripts/doc-gardening.sh` 재실행으로 확인
4. 커밋 + PR

## 리뷰 코멘트 판단 기준

리뷰 봇이 심각도 태그를 붙여 코멘트한다. 태그별 행동:

| 태그 | 행동 |
|---|---|
| `[P0-Bug]` `[P0-Arch]` | **반드시 수정** (버그/보안/아키텍처 위반) |
| `[P1-Concurrency]` `[P1-Test]` | **수정 권장** (동시성/테스트 — docs/RELIABILITY.md 기준 판단) |
| `[P2-Suggestion]` | **판단 필요**: (1) core-beliefs에 근거? (2) PR 범위 내? (3) 기존 패턴과 일치? |
| `[P2-Style]` | **무시** |

거부 시 답글: `이 제안은 수용하지 않습니다. 근거: [core-beliefs #N] ...`

## 코딩 표준

- 한국어 응답 및 문서 작성
- 들여쓰기 깊이 2단계 이내 (3단계 이상은 리팩터링 검토)
- 메서드 15~20줄 이내
- 도메인 의미가 있는 원시값은 값 객체로 래핑
- else보다 Early Return / Guard Clause 선호
- 경계에서 데이터 파싱 (Zod 스타일 검증)
- UTF-8 (BOM 제외)

## 기존 자동화 도구

- **CodeRabbit**: PR 자동 리뷰 (`.coderabbit.yaml`)
- **PR Agent**: PR 설명/리뷰 자동 생성 (`.pr_agent.toml`)
- **GitHub Actions — test.yml**: PR 시 컴파일 + 아키텍처/도메인 테스트 + 문서 검증 자동 실행
- **GitHub Actions — deploy.yml**: master push 시 Docker 빌드 → 배포
- **ArchUnit**: 아키텍처 규칙 테스트 자동 실행 (에이전트용 수정 지침 포함)
