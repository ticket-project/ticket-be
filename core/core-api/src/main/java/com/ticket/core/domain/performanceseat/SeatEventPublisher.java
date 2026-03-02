package com.ticket.core.domain.performanceseat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * 좌석 상태 변경 이벤트를 WebSocket으로 브로드캐스트하는 유틸 컴포넌트.
 * destination: /topic/performance/{performanceId}/seats
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeatEventPublisher {

    private static final String SEAT_TOPIC_FORMAT = "/topic/performance/%d/seats";

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 좌석 선택 이벤트를 해당 공연 회차를 구독 중인 모든 클라이언트에게 브로드캐스트합니다.
     */
    public void publishSeatSelected(final Long performanceId, final Long seatId, final Long memberId) {
        final SeatStatusMessage message = SeatStatusMessage.selected(performanceId, seatId, memberId);
        final String destination = String.format(SEAT_TOPIC_FORMAT, performanceId);
        messagingTemplate.convertAndSend(destination, message);
        log.info("좌석 선택 이벤트 발행: performanceId={}, seatId={}, memberId={}", performanceId, seatId, memberId);
    }

    /**
     * 좌석 선택 해제 이벤트를 해당 공연 회차를 구독 중인 모든 클라이언트에게 브로드캐스트합니다.
     */
    public void publishSeatDeselected(final Long performanceId, final Long seatId, final Long memberId) {
        final SeatStatusMessage message = SeatStatusMessage.deselected(performanceId, seatId, memberId);
        final String destination = String.format(SEAT_TOPIC_FORMAT, performanceId);
        messagingTemplate.convertAndSend(destination, message);
        log.info("좌석 해제 이벤트 발행: performanceId={}, seatId={}, memberId={}", performanceId, seatId, memberId);
    }
}
