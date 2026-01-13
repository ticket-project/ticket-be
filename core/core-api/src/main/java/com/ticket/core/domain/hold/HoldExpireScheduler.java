package com.ticket.core.domain.hold;

import com.ticket.core.domain.performanceseat.PerformanceSeat;
import com.ticket.core.domain.performanceseat.PerformanceSeatRepository;
import com.ticket.core.enums.PerformanceSeatState;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

//@Component
public class HoldExpireScheduler {

    private final PerformanceSeatRepository performanceSeatRepository;

    public HoldExpireScheduler(final PerformanceSeatRepository performanceSeatRepository) {
        this.performanceSeatRepository = performanceSeatRepository;
    }

    @Transactional
    @Scheduled(fixedRate = 60000)
    public void expireSeatHolds() {
        final List<PerformanceSeat> expiredSeatHolds = performanceSeatRepository.findAllByStateEquals(PerformanceSeatState.HELD);
        if (expiredSeatHolds.isEmpty()) {
            return;
        }
        expiredSeatHolds.forEach(PerformanceSeat::release);
    }
}
