name: ✨ 기능 요청
description: 새로운 기능이나 개선 사항을 제안해주세요.
title: "[Feature] "
labels: ["enhancement", "needs-review"]
assignees: []

body:
- type: markdown
  attributes:
  value: |
  새로운 기능 제안을 환영합니다! 아래 항목들을 작성해주시면 검토에 큰 도움이 됩니다.

- type: textarea
  id: problem
  attributes:
  label: 🤔 해결하고자 하는 문제
  description: 어떤 문제나 불편함을 경험하셨나요?
  placeholder: |
  예: 현재 좌석 선점 시간이 5분으로 고정되어 있어, 결제 시간이 오래 걸리는 사용자에게는 부족합니다.
  validations:
  required: true

- type: textarea
  id: solution
  attributes:
  label: 💡 제안하는 해결 방법
  description: 어떤 기능이나 개선이 이 문제를 해결할 수 있을까요?
  placeholder: |
  예: 사용자가 선택한 결제 수단에 따라 선점 시간을 동적으로 조정하는 기능을 추가합니다.
  - 간편 결제: 5분
  - 카드 결제: 10분
  - 계좌이체: 15분
  validations:
  required: true

- type: dropdown
  id: component
  attributes:
  label: 🎯 관련 컴포넌트
  description: 어느 영역과 관련된 기능인가요?
  multiple: true
  options:
  - 인증/회원 (Auth/Member)
  - 공연 관리 (Performance)
  - 좌석 관리 (Seat)
  - 예매 (Reservation)
  - 선점 (Hold)
  - 결제 (Payment)
  - 동시성 처리 (Concurrency)
  - API/컨트롤러
  - 데이터베이스
  - 스케줄러
  - 성능 최적화
  - 모니터링/로깅
  - 기타
  validations:
  required: true

- type: dropdown
  id: priority
  attributes:
  label: ⚡ 우선순위
  description: 이 기능의 중요도는 어느 정도인가요?
  options:
  - High (핵심 기능, 빠른 구현 필요)
  - Medium (유용하지만 필수는 아님)
  - Low (있으면 좋은 기능)
  validations:
  required: true

- type: textarea
  id: alternatives
  attributes:
  label: 🔄 대안
  description: 다른 해결 방법을 고려해보셨나요?
  placeholder: |
  예: Redis를 사용한 TTL 기반 선점 관리도 고려해볼 수 있습니다.
  validations:
  required: false

- type: textarea
  id: implementation
  attributes:
  label: 🛠️ 구현 아이디어 (선택)
  description: 기술적인 구현 방법에 대한 아이디어가 있다면 공유해주세요.
  placeholder: |
  예:
  - SeatHoldEntity에 holdDuration 필드 추가
  - PaymentType enum 추가 및 duration 매핑 테이블 생성
  - HoldExpireScheduler 로직 수정
  validations:
  required: false

- type: textarea
  id: additional
  attributes:
  label: 📎 추가 정보
  description: 참고 자료, 다른 시스템 사례, 또는 기타 참고 사항이 있다면 작성해주세요.
  validations:
  required: false