package com.ticket.core.domain.seathold;

import com.ticket.core.enums.HoldState;
import com.ticket.core.enums.PerformanceSeatState;
import com.ticket.storage.db.core.PerformanceSeatRepository;
import com.ticket.storage.db.core.SeatHoldEntity;
import com.ticket.storage.db.core.SeatHoldRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class HoldExpireScheduler {

    private final SeatHoldRepository seatHoldRepository;
    private final PerformanceSeatRepository performanceSeatRepository;

    public HoldExpireScheduler(final SeatHoldRepository seatHoldRepository, final PerformanceSeatRepository performanceSeatRepository) {
        this.seatHoldRepository = seatHoldRepository;
        this.performanceSeatRepository = performanceSeatRepository;
    }

    @Transactional
    @Scheduled(fixedRate = 60000)
    public void expireSeatHolds() {
        final LocalDateTime now = LocalDateTime.now();
        final List<SeatHoldEntity> expiredSeatHolds = seatHoldRepository.findAllByExpireAtBeforeAndStateEquals(now, HoldState.PENDING);
        if (expiredSeatHolds.isEmpty()) {
            return;
        }
        expiredSeatHolds.forEach(expiredSeatHold -> expiredSeatHold.restoreState(HoldState.RESTORED));
        final List<Long> performanceSeatIds = expiredSeatHolds.stream()
                .map(SeatHoldEntity::getPerformanceSeatId)
                .toList();
        performanceSeatRepository.changeStateToAvailable(performanceSeatIds, PerformanceSeatState.AVAILABLE);
    }
}
