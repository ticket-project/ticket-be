package com.ticket.core.api.controller;

import com.ticket.core.domain.member.MemberPrincipal;
import com.ticket.core.domain.performanceseat.SeatEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * <h2>좌석 선택/해제 WebSocket 메시지 핸들러</h2>
 *
 * <p>좌석 선택 화면에서 실시간으로 좌석 선택 상태를 공유하기 위한 WebSocket(STOMP) 컨트롤러입니다.</p>
 *
 * <h3>🔌 WebSocket 연결 정보</h3>
 * <table>
 *   <tr><th>항목</th><th>값</th></tr>
 *   <tr><td>엔드포인트</td><td>{@code ws://서버주소/ws} (SockJS)</td></tr>
 *   <tr><td>프로토콜</td><td>STOMP over WebSocket</td></tr>
 *   <tr><td>인증</td><td>STOMP CONNECT 프레임의 {@code Authorization: Bearer {JWT}} 헤더</td></tr>
 * </table>
 *
 * <h3>📡 구독 토픽</h3>
 * <pre>{@code /topic/performance/{performanceId}/seats}</pre>
 * <p>특정 공연 회차의 좌석 상태 변경 이벤트를 수신합니다.</p>
 *
 * <h3>📤 메시지 전송 (클라이언트 → 서버)</h3>
 * <table>
 *   <tr><th>동작</th><th>Destination</th><th>Payload</th></tr>
 *   <tr>
 *     <td>좌석 선택</td>
 *     <td>{@code /app/performance/{performanceId}/select-seat}</td>
 *     <td>{@code {"seatId": 42}}</td>
 *   </tr>
 *   <tr>
 *     <td>좌석 선택 해제</td>
 *     <td>{@code /app/performance/{performanceId}/deselect-seat}</td>
 *     <td>{@code {"seatId": 42}}</td>
 *   </tr>
 * </table>
 *
 * <h3>📥 수신 메시지 (서버 → 클라이언트)</h3>
 * <p>토픽 {@code /topic/performance/{performanceId}/seats}로 브로드캐스트되는 메시지:</p>
 * <pre>{@code
 * {
 *   "performanceId": 1,
 *   "seatId": 42,
 *   "action": "SELECTED",        // "SELECTED" 또는 "DESELECTED"
 *   "memberId": 1,               // 선택한 사용자 ID (비인증 시 null)
 *   "timestamp": "2026-03-02T16:00:00"
 * }
 * }</pre>
 *
 * <h3>💻 프론트엔드 연동 예시 (JavaScript)</h3>
 * <pre>{@code
 * // 의존성: sockjs-client, @stomp/stompjs (또는 stompjs)
 * import SockJS from 'sockjs-client';
 * import { Stomp } from '@stomp/stompjs';
 *
 * const socket = new SockJS('http://localhost:8080/ws');
 * const stompClient = Stomp.over(socket);
 *
 * // 1. 연결 (JWT 토큰 포함)
 * stompClient.connect(
 *   { Authorization: 'Bearer ' + accessToken },
 *   () => {
 *     // 2. 좌석 상태 구독
 *     stompClient.subscribe('/topic/performance/1/seats', (message) => {
 *       const data = JSON.parse(message.body);
 *       if (data.action === 'SELECTED') {
 *         disableSeat(data.seatId);      // 해당 좌석 비활성화
 *       } else if (data.action === 'DESELECTED') {
 *         enableSeat(data.seatId);       // 해당 좌석 다시 활성화
 *       }
 *     });
 *
 *     // 3. 좌석 선택 전송
 *     stompClient.send(
 *       '/app/performance/1/select-seat',
 *       {},
 *       JSON.stringify({ seatId: 42 })
 *     );
 *
 *     // 4. 좌석 선택 해제
 *     stompClient.send(
 *       '/app/performance/1/deselect-seat',
 *       {},
 *       JSON.stringify({ seatId: 42 })
 *     );
 *   },
 *   (error) => {
 *     console.error('WebSocket 연결 실패:', error);
 *   }
 * );
 *
 * // 연결 해제
 * stompClient.disconnect();
 * }</pre>
 *
 * @see SeatEventPublisher
 * @see com.ticket.core.domain.performanceseat.SeatStatusMessage
 * @see com.ticket.core.config.WebSocketConfig
 * @see com.ticket.core.config.security.WebSocketAuthInterceptor
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class SeatWebSocketController {

    private final SeatEventPublisher seatEventPublisher;

    /**
     * 좌석 선택 메시지를 수신하여, 같은 공연 회차를 구독 중인 모든 클라이언트에게 브로드캐스트합니다.
     *
     * @param performanceId 공연 회차 ID (URL 경로 변수)
     * @param payload       선택할 좌석 정보 ({@code {"seatId": 42}})
     * @param principal     STOMP CONNECT 시 인증된 사용자 (JWT 미전송 시 null)
     */
    @MessageMapping("/performance/{performanceId}/select-seat")
    public void selectSeat(
            @DestinationVariable final Long performanceId,
            @Payload final SeatSelectionPayload payload,
            final Principal principal
    ) {
        final Long memberId = extractMemberId(principal);
        if (memberId == null) {
            log.warn("비인가 사용자의 좌석 선택 요청 차단: performanceId={}, seatId={}", performanceId, payload.seatId());
            return;
        }
        
        log.info("좌석 선택 요청: performanceId={}, seatId={}, memberId={}", performanceId, payload.seatId(), memberId);
        seatEventPublisher.publishSeatSelected(performanceId, payload.seatId(), memberId);
    }

    /**
     * 좌석 선택 해제 메시지를 수신하여, 같은 공연 회차를 구독 중인 모든 클라이언트에게 브로드캐스트합니다.
     *
     * @param performanceId 공연 회차 ID (URL 경로 변수)
     * @param payload       해제할 좌석 정보 ({@code {"seatId": 42}})
     * @param principal     STOMP CONNECT 시 인증된 사용자 (JWT 미전송 시 null)
     */
    @MessageMapping("/performance/{performanceId}/deselect-seat")
    public void deselectSeat(
            @DestinationVariable final Long performanceId,
            @Payload final SeatSelectionPayload payload,
            final Principal principal
    ) {
        final Long memberId = extractMemberId(principal);
        if (memberId == null) {
            log.warn("비인가 사용자의 좌석 해제 요청 차단: performanceId={}, seatId={}", performanceId, payload.seatId());
            return;
        }

        log.info("좌석 해제 요청: performanceId={}, seatId={}, memberId={}", performanceId, payload.seatId(), memberId);
        seatEventPublisher.publishSeatDeselected(performanceId, payload.seatId(), memberId);
    }

    private Long extractMemberId(final Principal principal) {
        if (principal instanceof UsernamePasswordAuthenticationToken auth
                && auth.getPrincipal() instanceof MemberPrincipal memberPrincipal) {
            return memberPrincipal.getMemberId();
        }
        return null;
    }

    /**
     * 클라이언트에서 전송하는 좌석 선택/해제 페이로드.
     *
     * @param seatId 선택하거나 해제할 좌석 ID
     */
    public record SeatSelectionPayload(Long seatId) {}
}
