package com.ticket.core.infra.redis;

import com.ticket.core.domain.performanceseat.command.SeatEventPort;
import com.ticket.core.domain.performanceseat.support.SeatRedisKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.ticket.core.domain.performanceseat.support.SeatStatusMessage.SeatAction.DESELECTED;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeatSelectionExpirationHandler implements RedisKeyExpirationHandler {

    private final SeatEventPort seatEventPort;

    @Override
    public boolean supports(final String expiredKey) {
        return SeatRedisKey.tryParseSelectKey(expiredKey).isPresent();
    }

    @Override
    public void handle(final String expiredKey) {
        final SeatRedisKey.SelectKey selectKey = SeatRedisKey.tryParseSelectKey(expiredKey)
                .orElseThrow(() -> new IllegalArgumentException("吏?먰븯吏 ?딅뒗 醫뚯꽍 ?좏깮 留뚮즺 ?ㅼ엯?덈떎: " + expiredKey));

        seatEventPort.publish(selectKey.performanceId(), selectKey.seatId(), DESELECTED);
        log.info("醫뚯꽍 ?좏깮 留뚮즺 ?대깽??泥섎━: performanceId={}, seatId={}",
                selectKey.performanceId(), selectKey.seatId());
    }
}
