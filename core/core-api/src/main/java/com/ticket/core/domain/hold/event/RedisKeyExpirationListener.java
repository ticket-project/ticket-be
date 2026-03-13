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

        final SeatRedisKey.SelectKey selectKey = SeatRedisKey.tryParseSelectKey(expiredKey).orElse(null);
        if (selectKey != null) {
            handleSeatSelectionExpired(selectKey);
            return;
        }

        final SeatRedisKey.HoldMetaKey holdMetaKey = SeatRedisKey.tryParseHoldMetaKey(expiredKey).orElse(null);
        if (holdMetaKey != null) {
            handleHoldExpired(holdMetaKey);
        }
    }

    private void handleSeatSelectionExpired(final SeatRedisKey.SelectKey selectKey) {
        seatEventPublisher.publish(SeatStatusMessage.of(selectKey.performanceId(), selectKey.seatId(), DESELECTED));
        log.info("좌석 선택 만료 이벤트 처리: performanceId={}, seatId={}",
                selectKey.performanceId(), selectKey.seatId());
    }

    private void handleHoldExpired(final SeatRedisKey.HoldMetaKey holdMetaKey) {
        terminateOrderUseCase.expireByHoldKey(holdMetaKey.holdKey(), LocalDateTime.now());
        log.info("홀드 만료 이벤트 처리: holdKey={}", holdMetaKey.holdKey());
    }
}
