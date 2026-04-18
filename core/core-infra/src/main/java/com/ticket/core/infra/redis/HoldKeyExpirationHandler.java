package com.ticket.core.infra.redis;

import com.ticket.core.domain.order.command.expire.ExpireOrderUseCase;
import com.ticket.core.domain.performanceseat.support.SeatRedisKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class HoldKeyExpirationHandler implements RedisKeyExpirationHandler {

    private final ExpireOrderUseCase expireOrderUseCase;
    private final Clock clock;

    @Override
    public boolean supports(final String expiredKey) {
        return SeatRedisKey.tryParseHoldMetaKey(expiredKey).isPresent();
    }

    @Override
    public void handle(final String expiredKey) {
        final SeatRedisKey.HoldMetaKey holdMetaKey = SeatRedisKey.tryParseHoldMetaKey(expiredKey)
                .orElseThrow(() -> new IllegalArgumentException("吏?먰븯吏 ?딅뒗 ???留뚮즺 ?ㅼ엯?덈떎: " + expiredKey));

        expireOrderUseCase.expireByHoldKey(holdMetaKey.holdKey(), LocalDateTime.now(clock));
        log.info("???留뚮즺 ?대깽??泥섎━: holdKey={}", holdMetaKey.holdKey());
    }
}
