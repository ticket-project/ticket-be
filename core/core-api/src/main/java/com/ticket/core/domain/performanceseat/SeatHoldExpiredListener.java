package com.ticket.core.domain.performanceseat;

import com.ticket.core.domain.order.Order;
import com.ticket.core.domain.order.OrderSeat;
import com.ticket.core.domain.order.OrderSeatRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Redis Keyspace Notification 기반 Hold TTL 만료 리스너.
 * seat:hold:* 키가 만료되면 WebSocket RELEASED 이벤트를 발행하고,
 * 해당 좌석에 연결된 PENDING 주문을 자동 취소합니다.
 */
@Slf4j
@Component
public class SeatHoldExpiredListener extends KeyExpirationEventMessageListener {

    private static final String HOLD_KEY_PREFIX = "seat:hold:";

    private final SeatEventPublisher seatEventPublisher;
    private final OrderSeatRepository orderSeatRepository;

    public SeatHoldExpiredListener(
            final RedisMessageListenerContainer listenerContainer,
            final SeatEventPublisher seatEventPublisher,
            final OrderSeatRepository orderSeatRepository
    ) {
        super(listenerContainer);
        this.seatEventPublisher = seatEventPublisher;
        this.orderSeatRepository = orderSeatRepository;
    }

    @Override
    @Transactional
    public void onMessage(final Message message, final byte[] pattern) {
        final String expiredKey = message.toString();

        if (!expiredKey.startsWith(HOLD_KEY_PREFIX)) {
            return;
        }

        try {
            // 키 형식: seat:hold:{perf:1}:42
            final Long seatId = SeatRedisKey.extractSeatId(expiredKey);
            final Long performanceId = extractPerformanceId(expiredKey);

            log.info("Hold TTL 만료: performanceId={}, seatId={}", performanceId, seatId);

            // PENDING 주문 자동 취소
            cancelPendingOrders(seatId);

            seatEventPublisher.publish(
                    SeatStatusMessage.of(performanceId, seatId, SeatStatusMessage.SeatAction.RELEASED)
            );
        } catch (final Exception e) {
            log.error("Hold 만료 이벤트 처리 실패: key={}", expiredKey, e);
        }
    }

    /**
     * 만료된 좌석에 연결된 PENDING 주문을 취소합니다.
     */
    private void cancelPendingOrders(final Long performanceSeatId) {
        final List<OrderSeat> orderSeats = orderSeatRepository.findByPerformanceSeatIdIn(List.of(performanceSeatId));
        for (final OrderSeat orderSeat : orderSeats) {
            final Order order = orderSeat.getOrder();
            if (order.isPending()) {
                order.cancel();
                log.info("Hold 만료로 PENDING 주문 자동 취소: orderId={}, orderNo={}", order.getId(), order.getOrderNo());
            }
        }
    }

    /**
     * 키에서 performanceId를 추출합니다.
     * 키 형식: seat:hold:{perf:N}:seatId
     */
    private Long extractPerformanceId(final String key) {
        // "seat:hold:{perf:" 이후 "}" 이전의 숫자 추출
        final int start = key.indexOf("{perf:") + "{perf:".length();
        final int end = key.indexOf("}", start);
        return Long.parseLong(key.substring(start, end));
    }
}

