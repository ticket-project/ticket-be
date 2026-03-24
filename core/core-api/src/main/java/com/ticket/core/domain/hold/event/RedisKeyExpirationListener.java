package com.ticket.core.domain.hold.event;

import com.ticket.core.domain.order.expire.ExpireOrderUseCase;
import com.ticket.core.domain.queue.command.QueueAdmissionProcessor;
import com.ticket.core.domain.queue.runtime.QueueRedisKey;
import com.ticket.core.domain.queue.usecase.QueueEntryId;
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
    private final ExpireOrderUseCase expireOrderUseCase;
    private final QueueAdmissionProcessor queueAdmissionProcessor;

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
            return;
        }

        final QueueRedisKey.TokenKey tokenKey = QueueRedisKey.tryParseTokenStorageKey(expiredKey).orElse(null);
        if (tokenKey != null) {
            handleQueueTokenExpired(tokenKey);
        }
    }

    private void handleSeatSelectionExpired(final SeatRedisKey.SelectKey selectKey) {
        seatEventPublisher.publish(SeatStatusMessage.of(selectKey.performanceId(), selectKey.seatId(), DESELECTED));
        log.info("좌석 선택 만료 이벤트 처리: performanceId={}, seatId={}",
                selectKey.performanceId(), selectKey.seatId());
    }

    private void handleHoldExpired(final SeatRedisKey.HoldMetaKey holdMetaKey) {
        expireOrderUseCase.expireByHoldKey(holdMetaKey.holdKey(), LocalDateTime.now());
        log.info("홀드 만료 이벤트 처리: holdKey={}", holdMetaKey.holdKey());
    }

    private void handleQueueTokenExpired(final QueueRedisKey.TokenKey tokenKey) {
        final String queueToken = tokenKey.performanceId() + ":" + tokenKey.queueEntryId() + ":" + tokenKey.tokenId();
        queueAdmissionProcessor.handleTokenExpired(tokenKey.performanceId(), QueueEntryId.from(tokenKey.queueEntryId()), queueToken);
        log.info("대기열 토큰 만료 이벤트 처리: performanceId={}, queueEntryId={}",
                tokenKey.performanceId(), tokenKey.queueEntryId());
    }
}
