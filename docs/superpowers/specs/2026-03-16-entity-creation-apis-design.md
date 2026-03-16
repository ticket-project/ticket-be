# Entity Creation APIs Design

**목표**
- 테스트에서 reflection 기반 엔티티 조립을 제거할 수 있도록 메인 코드에 정상 생성 경로를 추가한다.

**범위**
- `Venue`, `Category`, `Performer`, `ShowGrade`, `ShowSeat`에 정적 팩토리를 추가한다.
- 기존 테스트 fixture와 단위 테스트를 새 생성 경로로 치환한다.
- JPA 식별자 주입처럼 테스트에서 불가피한 reflection만 남긴다.

**설계**
- JPA 기본 생성자는 `protected`로 유지한다.
- 생성 의도가 드러나는 정적 메서드 이름을 사용한다.
- 필수 필드는 팩토리에서 모두 채우고, 테스트가 필드명을 알 필요 없게 만든다.

**예상 효과**
- fixture 코드 길이 감소
- 필드명 변경에 대한 테스트 취약성 감소
- 테스트가 구현 세부사항보다 도메인 의미를 더 직접 표현
