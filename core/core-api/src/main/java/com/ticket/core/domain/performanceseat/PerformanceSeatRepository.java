package com.ticket.core.domain.performanceseat;

import com.ticket.core.domain.performance.Performance;
import com.ticket.core.domain.seat.Seat;
import com.ticket.core.enums.PerformanceSeatState;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.*;

import java.util.Collection;
import java.util.List;

public interface PerformanceSeatRepository extends JpaRepository<PerformanceSeat, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
            @QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")
    })
    List<PerformanceSeat> findByPerformanceIdAndSeatIdInAndState(Long performanceId, List<Long> seatIds, PerformanceSeatState state);

    @Modifying
    @Query("""
            UPDATE PerformanceSeat ps
            SET ps.state = :changeState
            WHERE ps.state = :curState
    """)
    int updateState(Long performanceId, List<Long> seatIds, PerformanceSeatState curState, PerformanceSeatState changeState);

    List<PerformanceSeat> findAllByPerformanceIdAndSeatIdIn(Long performanceId, Collection<Long> seatIds);

    List<PerformanceSeat> findAllByStateEquals(PerformanceSeatState state);

    List<PerformanceSeat> findAllByPerformanceAndSeatIn(Performance performance, Collection<Seat> seats);
}
