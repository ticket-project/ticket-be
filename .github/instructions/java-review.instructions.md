---
applyTo: "core/**/*.java"
---

# Java/Spring 리뷰 강화 지침

- Java 변경은 패치만 보지 말고 관련 service, repository, controller, test까지 함께 확인한다.
- `@Transactional` 경계, 읽기 전용 조회, 예외 처리, Optional 사용, null 반환, JPA fetch 전략을 우선 검토한다.
- Redis expiration listener, scheduler, distributed lock AOP와 연결된 Java 코드는 높은 우선순위로 본다.
- 인증/인가, JWT, refresh token, OAuth2, 공개 API 노출 위험은 항상 별도 확인한다.
- 테스트가 없다면 어떤 테스트가 빠졌는지 구체적으로 적는다.
