package com.ticket.core.domain.performanceseat.support;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * 좌석 상태 변경 이벤트를 WebSocket으로 브로드캐스트하는 컴포넌트.
 * 단일 서버 환경에서 SimpMessagingTemplate을 직접 사용합니다.
 * 스케일아웃 시 Redis Pub/Sub으로 전환이 필요합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeatEventPublisher {

    private static final String SEAT_TOPIC_FORMAT = "/topic/performance/%d/seats";

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 좌석 상태 변경 이벤트를 해당 공연 회차를 구독 중인 모든 클라이언트에게 브로드캐스트합니다.
     */
    public void publish(SeatStatusMessage message) {
        String destination = String.format(SEAT_TOPIC_FORMAT, message.performanceId());
        messagingTemplate.convertAndSend(destination, message);
        log.info("좌석 이벤트 발행: action={}, perfId={}, seatId={}",
                message.action(), message.performanceId(), message.seatId());
    }
}
