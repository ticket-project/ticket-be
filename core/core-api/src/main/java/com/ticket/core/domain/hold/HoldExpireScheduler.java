package com.ticket.core.domain.hold;

import com.ticket.core.enums.PerformanceSeatState;
import com.ticket.storage.db.core.PerformanceSeatEntity;
import com.ticket.storage.db.core.PerformanceSeatRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class HoldExpireScheduler {

    private final PerformanceSeatRepository performanceSeatRepository;

    public HoldExpireScheduler(final PerformanceSeatRepository performanceSeatRepository) {
        this.performanceSeatRepository = performanceSeatRepository;
    }

    @Transactional
    @Scheduled(fixedRate = 60000)
    public void expireSeatHolds() {
        final LocalDateTime now = LocalDateTime.now();
        final List<PerformanceSeatEntity> expiredSeatHolds = performanceSeatRepository.findAllByHoldExpireAtBeforeAndStateEquals(now, PerformanceSeatState.HELD);
        if (expiredSeatHolds.isEmpty()) {
            return;
        }
        expiredSeatHolds.forEach(PerformanceSeatEntity::release);
    }
}
