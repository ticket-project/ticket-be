name: 🐛 버그 리포트
description: 버그나 예상치 못한 동작을 발견하셨나요?
title: "[Bug] "
labels: ["bug", "needs-triage"]
assignees: []

body:
- type: markdown
  attributes:
  value: |
  버그 리포트를 작성해주셔서 감사합니다. 가능한 한 자세히 작성해주시면 빠른 해결에 도움이 됩니다.

- type: textarea
  id: description
  attributes:
  label: 🔍 버그 설명
  description: 어떤 버그가 발생했는지 명확하고 간결하게 설명해주세요.
  placeholder: |
  예: 동일한 좌석을 여러 사용자가 동시에 예매할 수 있는 문제가 발생합니다.
  validations:
  required: true

- type: textarea
  id: reproduction
  attributes:
  label: 📝 재현 방법
  description: 버그를 재현할 수 있는 단계를 상세히 작성해주세요.
  placeholder: |
  1. 두 개의 클라이언트에서 동시에 로그인
  2. 같은 공연의 같은 좌석 선택
  3. 동시에 예매 버튼 클릭
  4. 두 예매 모두 성공하는 문제 발생
  validations:
  required: true

- type: textarea
  id: expected
  attributes:
  label: ✅ 예상 동작
  description: 어떤 동작이 예상되었나요?
  placeholder: |
  예: 한 명의 예매만 성공하고, 나머지는 실패해야 합니다.
  validations:
  required: true

- type: textarea
  id: actual
  attributes:
  label: ❌ 실제 동작
  description: 실제로는 어떤 동작이 발생했나요?
  placeholder: |
  예: 두 명 모두 예매가 성공했습니다.
  validations:
  required: true

- type: dropdown
  id: severity
  attributes:
  label: 🚨 심각도
  description: 이 버그의 영향도는 어느 정도인가요?
  options:
  - Critical (시스템 전체에 영향, 즉시 수정 필요)
  - High (주요 기능 장애, 빠른 수정 필요)
  - Medium (기능 일부 제한, 우회 방법 존재)
  - Low (사소한 문제, 사용자 경험에 미미한 영향)
  validations:
  required: true

- type: dropdown
  id: component
  attributes:
  label: 🎯 관련 컴포넌트
  description: 어느 영역에서 발생한 버그인가요?
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
  - 기타
  validations:
  required: true

- type: textarea
  id: environment
  attributes:
  label: 🖥️ 환경 정보
  description: 버그가 발생한 환경을 알려주세요.
  placeholder: |
  - OS: Ubuntu 22.04
  - Java: 17
  - Spring Boot: 3.x
  - Database: MySQL 8.0
  - 브라우저: Chrome 120 (프론트엔드 관련 시)
  validations:
  required: false

- type: textarea
  id: logs
  attributes:
  label: 📋 로그 및 에러 메시지
  description: 관련된 로그나 에러 메시지를 첨부해주세요.
  render: shell
  placeholder: |
  예시:
  java.lang.RuntimeException: Seat already reserved
  at com.ticket.core.domain.reservation.ReservationService.addReservation(...)
  validations:
  required: false

- type: textarea
  id: additional
  attributes:
  label: 📎 추가 정보
  description: 스크린샷, 관련 PR, 또는 기타 참고 사항이 있다면 작성해주세요.
  validations:
  required: false