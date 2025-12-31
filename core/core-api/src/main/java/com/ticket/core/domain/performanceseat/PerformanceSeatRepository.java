package com.ticket.core.domain.performanceseat;

import com.ticket.core.enums.PerformanceSeatState;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.*;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface PerformanceSeatRepository extends JpaRepository<PerformanceSeat, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
            @QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")
    })
    List<PerformanceSeat> findByPerformanceIdAndSeatIdInAndState(Long performanceId, List<Long> seatIds, PerformanceSeatState State);

    @Modifying
    @Query("""
            UPDATE PerformanceSeat ps
            SET ps.state = :changeState
            WHERE ps.state = :curState
            AND ps.performanceId = :performanceId
            AND ps.seatId IN :seatIds
    """)
    int updateState(Long performanceId, List<Long> seatIds, PerformanceSeatState curState, PerformanceSeatState changeState);

    List<PerformanceSeat> findAllByPerformanceIdAndSeatIdIn(Long performanceId, Collection<Long> seatIds);

    List<PerformanceSeat> findAllByHoldExpireAtBeforeAndStateEquals(LocalDateTime expireAtBefore, PerformanceSeatState state);

    List<PerformanceSeat> findAllByStateEquals(PerformanceSeatState state);
}
