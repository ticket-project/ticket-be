package com.ticket.core.domain.hold.event;

import com.ticket.core.domain.order.command.usecase.TerminateOrderUseCase;
import com.ticket.core.domain.performanceseat.support.SeatEventPublisher;
import com.ticket.core.domain.performanceseat.support.SeatRedisKey;
import com.ticket.core.domain.performanceseat.support.SeatStatusMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static com.ticket.core.domain.performanceseat.support.SeatStatusMessage.SeatAction.DESELECTED;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisKeyExpirationListener implements MessageListener {

    private final SeatEventPublisher seatEventPublisher;
    private final TerminateOrderUseCase terminateOrderUseCase;

    @Override
    public void onMessage(final Message message, final byte[] pattern) {
        final String expiredKey = new String(message.getBody(), StandardCharsets.UTF_8);

        if (SeatRedisKey.isSelectKey(expiredKey)) {
            handleSeatSelectionExpired(expiredKey);
            return;
        }

        if (SeatRedisKey.isHoldMetaKey(expiredKey)) {
            handleHoldExpired(expiredKey);
        }
    }

    private void handleSeatSelectionExpired(final String expiredKey) {
        final Long performanceId = SeatRedisKey.extractPerformanceId(expiredKey);
        final Long seatId = SeatRedisKey.extractSeatId(expiredKey);
        seatEventPublisher.publish(SeatStatusMessage.of(performanceId, seatId, DESELECTED));
        log.info("좌석 선택 만료 이벤트 처리: performanceId={}, seatId={}", performanceId, seatId);
    }

    private void handleHoldExpired(final String expiredKey) {
        final String holdToken = SeatRedisKey.extractHoldToken(expiredKey);
        terminateOrderUseCase.expireByHoldToken(holdToken, LocalDateTime.now());
        log.info("홀드 만료 이벤트 처리: holdToken={}", holdToken);
    }
}
