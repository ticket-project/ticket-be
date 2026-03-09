# 좌석 실시간 WebSocket 연동 가이드

## 1. 목적
공연 회차(`performanceId`) 단위의 좌석 상태 변경 이벤트를 실시간으로 구독한다.

중요:
- 현재 WebSocket은 `구독 전용`이다.
- 좌석 선택/해제 같은 상태 변경은 STOMP publish가 아니라 `REST API`로 수행한다.

## 2. 연결 정보
| 항목 | 값 |
|---|---|
| 엔드포인트 | `/ws` |
| 전송 방식 | SockJS + STOMP |
| 브로커 prefix | `/topic` |
| 클라이언트 publish prefix | 없음 |
| 인증 방식 | STOMP `CONNECT` frame의 `Authorization: Bearer {JWT}` |

예시 URL:
- 로컬: `http://localhost:8080/ws`
- 운영: `https://{host}/ws`

주의:
- 서버는 `CONNECT` 시점에 JWT를 검사한다.
- Authorization 헤더가 없거나 토큰이 유효하지 않으면 연결이 거부된다.
- `setApplicationDestinationPrefixes("/app")` 설정이 없으므로 `/app/**` 로 publish하면 동작하지 않는다.

## 3. 구독 채널
구독 destination:

```text
/topic/performance/{performanceId}/seats
```

예:

```text
/topic/performance/1/seats
```

## 4. 메시지 스펙
서버가 브로드캐스트하는 메시지 형식:

```json
{
  "performanceId": 1,
  "seatId": 42,
  "action": "SELECTED",
  "timestamp": "2026-03-09T10:15:30.123"
}
```

필드 설명:
- `performanceId` (`number`): 공연 회차 ID
- `seatId` (`number`): 좌석 ID
- `action` (`string`): 좌석 상태 변경 이벤트
- `timestamp` (`string`): 서버 이벤트 발생 시각, ISO-8601

현재 서버가 보내는 `action` 값:
- `SELECTED`: 좌석 선택
- `DESELECTED`: 좌석 선택 해제
- `HELD`: 좌석 홀드
- `RELEASED`: 좌석 홀드 해제
- `RESERVED`: 좌석 예약 완료

주의:
- 현재 메시지에는 `memberId`가 포함되지 않는다.
- 클라이언트는 `action + seatId` 기준으로 UI 상태를 갱신해야 한다.

## 5. 상태 변경은 REST API로 수행
현재 구현에서는 WebSocket으로 좌석 변경 요청을 보내지 않는다.

좌석 선택:

```http
POST /api/v1/performances/{performanceId}/seats/{seatId}/select
Authorization: Bearer {JWT}
```

좌석 선택 해제:

```http
DELETE /api/v1/performances/{performanceId}/seats/{seatId}/select
Authorization: Bearer {JWT}
```

서버는 REST 처리 성공 후 WebSocket 구독자들에게 이벤트를 브로드캐스트한다.

## 6. 초기 로딩용 REST API
실시간 구독 전에 아래 API로 기본 데이터를 먼저 조회한다.

- `GET /api/v1/shows/{showId}/venue-layout`
- `GET /api/v1/shows/{showId}/seats`
- `GET /api/v1/performances/{performanceId}/seats/status`
- `GET /api/v1/performances/{performanceId}/seats/availability`

권장 순서:
1. 공연장 레이아웃 조회
2. 좌석 메타데이터 조회
3. 현재 좌석 상태 조회
4. WebSocket 연결 및 구독
5. 좌석 선택/해제는 REST 호출
6. WebSocket 이벤트 수신 시 UI 반영

## 7. 프론트엔드 예시
`@stomp/stompjs` + `sockjs-client`

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
    client.subscribe(`/topic/performance/${performanceId}/seats`, (message: IMessage) => {
      const event = JSON.parse(message.body) as {
        performanceId: number;
        seatId: number;
        action: 'SELECTED' | 'DESELECTED' | 'HELD' | 'RELEASED' | 'RESERVED';
        timestamp: string;
      };

      switch (event.action) {
        case 'SELECTED':
        case 'HELD':
        case 'RESERVED':
          // 좌석 비활성화 처리
          break;
        case 'DESELECTED':
        case 'RELEASED':
          // 좌석 활성화 처리
          break;
      }
    });
  },
  onStompError: (frame) => {
    console.error('STOMP ERROR', frame.headers['message'], frame.body);
  },
  onWebSocketError: (event) => {
    console.error('WS ERROR', event);
  },
});

client.activate();
```

좌석 변경 예시:

```ts
export async function selectSeat(performanceId: number, seatId: number, accessToken: string) {
  await fetch(`/api/v1/performances/${performanceId}/seats/${seatId}/select`, {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${accessToken}`,
    },
  });
}

export async function deselectSeat(performanceId: number, seatId: number, accessToken: string) {
  await fetch(`/api/v1/performances/${performanceId}/seats/${seatId}/select`, {
    method: 'DELETE',
    headers: {
      Authorization: `Bearer ${accessToken}`,
    },
  });
}
```

## 8. 인증/에러 처리 권장사항
- WebSocket 연결 전에 Access Token 존재 여부를 확인한다.
- 토큰 만료 시 WebSocket을 재연결하지 말고 먼저 토큰을 재발급한다.
- `CONNECT` 실패는 인증 실패로 간주하고 로그인 또는 토큰 갱신 흐름으로 보낸다.
- REST 성공만 믿지 말고, 최종 UI 반영은 WebSocket 이벤트 또는 상태 재조회 결과로 보정하는 것이 안전하다.

## 9. 서버 구현 기준
- `WebSocketConfig`
  `/ws` endpoint 등록, Simple Broker `/topic` 설정
- `WebSocketAuthInterceptor`
  STOMP `CONNECT` 프레임의 JWT 인증 처리
- `SeatEventPublisher`
  `/topic/performance/{performanceId}/seats` 로 이벤트 브로드캐스트
- `SeatSelectionController`
  좌석 선택/해제 REST API 제공
