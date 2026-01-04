package com.ticket.core.domain.hold;

import com.ticket.core.domain.performanceseat.PerformanceSeat;
import com.ticket.core.support.ConcurrencyTestBase;
import com.ticket.core.support.ConcurrentTestUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

@SuppressWarnings("NonAsciiCharacters")
class HoldServiceV0ConcurrencyTest extends ConcurrencyTestBase {

    @Autowired
    @Qualifier("holdServiceV0")
    private HoldService holdService;

    @Test
    void 재고가_1개일때_여러_요청이_동시에_들어오면_비관적_락에_의해_한_명만_선점한다() throws InterruptedException {
        // given
        final List<Long> seatIds = savedPerformanceSeats.stream()
                .map(PerformanceSeat::getSeatId)
                .toList();
        // when & then
        ConcurrentTestUtil.execute(100, idx -> holdService.hold(new NewSeatHold(
                savedMembers.get(idx).getId(),
                savedPerformance.getId(),
                seatIds
        )));
    }

}
