package com.ticket.core.domain.hold.application;

import com.ticket.core.domain.hold.model.HoldHistory;
import com.ticket.core.domain.hold.repository.HoldHistoryRepository;
import com.ticket.core.domain.performanceseat.model.PerformanceSeat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class HoldHistoryRecorder {

    private final HoldHistoryRepository holdHistoryRepository;

    public void recordActiveHold(
            final Long memberId,
            final Long performanceId,
            final String holdKey,
            final LocalDateTime expiresAt,
            final List<PerformanceSeat> performanceSeats
    ) {
        final List<HoldHistory> holdHistories = performanceSeats.stream()
                .map(seat -> new HoldHistory(
                        holdKey,
                        memberId,
                        performanceId,
                        seat.getId(),
                        seat.getSeat().getId(),
                        expiresAt
                ))
                .toList();
        holdHistoryRepository.saveAll(holdHistories);
    }
}
