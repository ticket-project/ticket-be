# 좌석 실시간 WebSocket API 가이드

## 1. 연결 정보

| 항목 | 값 |
|---|---|
| **엔드포인트** | `ws://서버주소/ws` (SockJS) |
| **프로토콜** | STOMP over WebSocket |
| **인증** | STOMP CONNECT 프레임의 `Authorization: Bearer {JWT}` 헤더 |

---

## 2. 구독 (서버 → 클라이언트)

### 토픽
```
/topic/performance/{performanceId}/seats
```

### 수신 메시지 형식
```json
{
  "performanceId": 1,
  "seatId": 42,
  "action": "SELECTED",
  "memberId": 1,
  "timestamp": "2026-03-02T16:00:00"
}
```

| 필드 | 타입 | 설명 |
|---|---|---|
| `performanceId` | `Long` | 공연 회차 ID |
| `seatId` | `Long` | 좌석 ID |
| `action` | `String` | `"SELECTED"` 또는 `"DESELECTED"` |
| `memberId` | `Long?` | 선택한 사용자 ID (비인증 시 `null`) |
| `timestamp` | `String` | ISO 8601 시각 |

---

## 3. 전송 (클라이언트 → 서버)

### 좌석 선택
```
Destination: /app/performance/{performanceId}/select-seat
Payload: { "seatId": 42 }
```

### 좌석 선택 해제
```
Destination: /app/performance/{performanceId}/deselect-seat
Payload: { "seatId": 42 }
```

---

## 4. 프론트엔드 연동 예시

### 의존성
```bash
npm install sockjs-client @stomp/stompjs
```

### TypeScript/JavaScript 코드
```typescript
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

const performanceId = 1;
const accessToken = 'your-jwt-token';

// 1. 연결
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

stompClient.connect(
  { Authorization: `Bearer ${accessToken}` },
  () => {
    console.log('WebSocket 연결 성공');

    // 2. 좌석 상태 변경 구독
    stompClient.subscribe(
      `/topic/performance/${performanceId}/seats`,
      (message) => {
        const data = JSON.parse(message.body);
        if (data.action === 'SELECTED') {
          // 해당 좌석 비활성화 (다른 유저가 선택)
          disableSeat(data.seatId);
        } else if (data.action === 'DESELECTED') {
          // 해당 좌석 활성화 (다른 유저가 선택 해제)
          enableSeat(data.seatId);
        }
      }
    );

    // 3. 좌석 선택
    stompClient.send(
      `/app/performance/${performanceId}/select-seat`,
      {},
      JSON.stringify({ seatId: 42 })
    );

    // 4. 좌석 선택 해제
    stompClient.send(
      `/app/performance/${performanceId}/deselect-seat`,
      {},
      JSON.stringify({ seatId: 42 })
    );
  },
  (error) => {
    console.error('WebSocket 연결 실패:', error);
  }
);

// 연결 해제
// stompClient.disconnect();
```

---

## 5. 관련 REST API

| API | URL | 설명 |
|---|---|---|
| 공연장 레이아웃 | `GET /api/v1/shows/{showId}/venue-layout` | viewBox, 좌석 지름 |
| 좌석 정보 | `GET /api/v1/shows/{showId}/seats` | 등급/좌표/가격 |
| 좌석 상태 | `GET /api/v1/performances/{id}/seats/status` | 전체 좌석 상태 (초기 로드용) |
| 등급별 잔여석 | `GET /api/v1/performances/{id}/seats/availability` | 등급별 잔여 좌석 수 |

> **권장 흐름:** 페이지 진입 시 REST API로 초기 데이터 로드 → WebSocket 구독으로 실시간 업데이트 수신
