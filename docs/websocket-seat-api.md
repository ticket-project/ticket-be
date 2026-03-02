# 좌석 실시간 WebSocket 연동 가이드 (프론트 공유용)

## 1. 목표
좌석 선택/해제 상태를 관람 회차(`performanceId`) 단위로 실시간 동기화한다.

## 2. 연결 정보
| 항목 | 값 |
|---|---|
| 엔드포인트 | `/ws` (SockJS) |
| 프로토콜 | STOMP over WebSocket |
| 브로커 구독 prefix | `/topic` |
| 클라이언트 발행 prefix | `/app` |
| 인증 헤더 | STOMP `CONNECT` frame의 `Authorization: Bearer {JWT}` |

예시 URL:
- 로컬: `http://localhost:8080/ws`
- 운영: `https://{host}/ws`

## 3. 구독(서버 -> 클라이언트)
구독 destination:
```text
/topic/performance/{performanceId}/seats
```

메시지 스키마:
```json
{
  "performanceId": 1,
  "seatId": 42,
  "action": "SELECTED",
  "memberId": 1001,
  "timestamp": "2026-03-02T18:45:12.123"
}
```

필드 정의:
- `performanceId` (`number`): 회차 ID
- `seatId` (`number`): 좌석 ID
- `action` (`"SELECTED" | "DESELECTED"`): 좌석 상태 변경 이벤트
- `memberId` (`number`): 이벤트를 발생시킨 회원 ID
- `timestamp` (`string`, ISO-8601): 서버 이벤트 시각

## 4. 발행(클라이언트 -> 서버)
좌석 선택:
```text
/app/performance/{performanceId}/select-seat
```
payload:
```json
{ "seatId": 42 }
```

좌석 선택 해제:
```text
/app/performance/{performanceId}/deselect-seat
```
payload:
```json
{ "seatId": 42 }
```

## 5. 인증/권한 동작
- `CONNECT` 시 JWT가 유효하면 사용자 컨텍스트가 설정된다.
- JWT가 없거나 유효하지 않아도 **연결 자체는 성립**될 수 있다.
- 단, 인증 사용자가 아니면 `select-seat`/`deselect-seat` 요청은 서버에서 무시된다(브로드캐스트 없음).

프론트 권장:
- 연결 전 Access Token 존재 여부 확인
- 토큰 만료 시 재발급 후 재연결
- 발행 후 일정 시간 내 이벤트 미수신 시 실패 처리(낙관적 UI 롤백 등)

## 6. 프론트 예시 코드 (@stomp/stompjs)
```ts
import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const performanceId = 1;
const accessToken = 'YOUR_JWT';

const client = new Client({
  webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
  connectHeaders: {
    Authorization: `Bearer ${accessToken}`,
  },
  reconnectDelay: 3000,
  onConnect: () => {
    client.subscribe(
      `/topic/performance/${performanceId}/seats`,
      (message: IMessage) => {
        const body = JSON.parse(message.body);

        if (body.action === 'SELECTED') {
          // 좌석 비활성화 처리
        }

        if (body.action === 'DESELECTED') {
          // 좌석 활성화 처리
        }
      }
    );
  },
  onStompError: (frame) => {
    console.error('STOMP ERROR', frame.headers['message'], frame.body);
  },
  onWebSocketError: (event) => {
    console.error('WS ERROR', event);
  },
});

client.activate();

export const selectSeat = (seatId: number) => {
  client.publish({
    destination: `/app/performance/${performanceId}/select-seat`,
    body: JSON.stringify({ seatId }),
  });
};

export const deselectSeat = (seatId: number) => {
  client.publish({
    destination: `/app/performance/${performanceId}/deselect-seat`,
    body: JSON.stringify({ seatId }),
  });
};

// 필요 시
// client.deactivate();
```

## 7. 초기 데이터 로딩(REST)
실시간 동기화 전에 아래 API로 초기 상태를 먼저 조회한다.

- `GET /api/v1/shows/{showId}/venue-layout`
- `GET /api/v1/shows/{showId}/seats`
- `GET /api/v1/performances/{performanceId}/seats/status`
- `GET /api/v1/performances/{performanceId}/seats/availability`

권장 순서:
1. REST로 좌석/레이아웃/초기 상태 조회
2. WebSocket 연결 및 구독
3. 좌석 선택/해제 이벤트 발행
4. 수신 이벤트로 UI 동기화

## 8. 참고(백엔드 구현 기준)
- `WebSocketConfig`: `/ws`, `/app`, `/topic` 설정
- `WebSocketAuthInterceptor`: `CONNECT`의 `Authorization` JWT 파싱
- `SeatWebSocketController`: `select-seat`, `deselect-seat` 처리
- `SeatEventPublisher`: `/topic/performance/{performanceId}/seats` 브로드캐스트
