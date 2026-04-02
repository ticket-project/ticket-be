---
name: 버그 리포트 (자유 서술)
about: 구조화 폼으로 정리하기 어려운 버그를 자유 형식으로 제보합니다.
title: "[Bug] "
labels: bug, needs-triage
assignees: ""
---

## 문제 요약
- 무엇이 잘못됐는지 한 문단으로 요약해 주세요.

## 버그 유형
- 예: 기능 오동작 / 회귀 / 동시성 / 데이터 불일치 / 성능 저하 / 보안 / 배포 설정

## 관련 영역
- 예: Auth, Hold, Order, Seat, Queue, Redis Expiration, Lock, WebSocket, API, Database

## 관련 API / 클래스 / 잡 이름
- 예: `POST /api/v1/performances/{performanceId}/holds`
- 예: `StartOrderUseCase`, `RedisKeyExpirationListener`

## 사전 조건
- 재현 전에 필요한 데이터, 계정, Redis 상태, 주문 상태 등을 적어 주세요.

## 재현 절차
1. 단계 1
2. 단계 2
3. 단계 3

## 기대 결과
- 정상이라면 어떤 결과가 나와야 하나요?

## 실제 결과
- 실제로 어떤 응답, 상태 변화, 로그가 나왔나요?

## 영향 범위
- 사용자 영향
- 운영 영향
- 데이터 정합성 영향

## 심각도 / 발생 빈도 / 환경
- 심각도:
- 발생 빈도:
- 환경: local / dev / staging / production 유사 / CI

## 실행 환경 상세
- Profile:
- Branch:
- Commit:
- DB:
- Redis:
- Client:

## 추정 원인
- 의심되는 클래스, 락 범위, Redis TTL, listener/scheduler 흐름이 있으면 적어 주세요.

## 로그 / 에러 / 요청-응답 예시
```text
붙여 넣기
```

## 추가 참고
- 관련 이슈 / PR / 스크린샷 / 문서 링크
