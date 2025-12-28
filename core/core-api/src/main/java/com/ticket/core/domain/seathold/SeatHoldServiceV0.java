package com.ticket.core.domain.seathold;

import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.MemberFinder;
import com.ticket.core.enums.EntityStatus;
import com.ticket.core.enums.HoldState;
import com.ticket.core.enums.PerformanceSeatState;
import com.ticket.core.enums.PerformanceState;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import com.ticket.core.support.exception.NotFoundException;
import com.ticket.storage.db.core.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SeatHoldServiceV0 implements SeatHoldService {

    private final MemberFinder memberFinder;
    private final PerformanceRepository performanceRepository;
    private final PerformanceSeatRepository performanceSeatRepository;
    private final SeatHoldRepository seatHoldRepository;

    public SeatHoldServiceV0(final MemberFinder memberFinder,
                                final PerformanceRepository performanceRepository,
                                final PerformanceSeatRepository performanceSeatRepository,
                                final SeatHoldRepository seatHoldRepository
    ) {
        this.memberFinder = memberFinder;
        this.performanceRepository = performanceRepository;
        this.performanceSeatRepository = performanceSeatRepository;
        this.seatHoldRepository = seatHoldRepository;
    }

    @Override
    @Transactional
    public SeatHoldInfo hold(final NewSeatHold newSeatHold) {
        final Member foundMember = memberFinder.find(newSeatHold.getMemberId());
        final PerformanceEntity foundPerformance = findOpenPerformance(newSeatHold.getPerformanceId());
        final List<PerformanceSeatEntity> availablePerformanceSeats = findAvailablePerformanceSeats(newSeatHold.getSeatIds(), foundPerformance.getId());
        if (availablePerformanceSeats.isEmpty()) {
            throw new CoreException(ErrorType.NOT_FOUND_DATA, "가능한 좌석이 없습니다.");
        }
        if (availablePerformanceSeats.size() != newSeatHold.getSeatIds().size()) {
            throw new CoreException(ErrorType.SEAT_COUNT_MISMATCH);
        }
        for (PerformanceSeatEntity availablePerformanceSeat : availablePerformanceSeats) {
            if (availablePerformanceSeat.getState() != PerformanceSeatState.AVAILABLE) {
                throw new CoreException(ErrorType.NOT_FOUND_DATA, "가능한 좌석이 없습니다.");
            }
        }
        availablePerformanceSeats.forEach(PerformanceSeatEntity::hold);

        final List<SeatHoldEntity> seatHoldEntities = availablePerformanceSeats.stream()
                .map(m -> new SeatHoldEntity(foundMember.getId(), m.getId(), LocalDateTime.now().plusMinutes(5), HoldState.PENDING))
                .toList();
        seatHoldRepository.saveAll(seatHoldEntities);
        //응답값을 공연, 회차, 좌석정보, 선점 유효 시간(웹소켓으로?)
        return new SeatHoldInfo(foundPerformance.getId(), availablePerformanceSeats.stream().map(PerformanceSeatEntity::getId).toList(), foundMember.getId(), HoldState.PENDING);
        //할인 쿠폰이나 포인트 같은 가격은 결제 창에서.
    }

    private PerformanceEntity findOpenPerformance(final Long performanceId) {
        return performanceRepository.findByIdAndStateAndStatus(
                        performanceId,
                        PerformanceState.OPEN,
                        EntityStatus.ACTIVE
                )
                .orElseThrow(() -> new NotFoundException(ErrorType.NOT_FOUND_DATA));
    }

    private List<PerformanceSeatEntity> findAvailablePerformanceSeats(final List<Long> seatIds, final Long performanceId) {
        return performanceSeatRepository.findByPerformanceIdAndSeatIdInAndState(
                performanceId,
                seatIds,
                PerformanceSeatState.AVAILABLE
        );
    }
}
