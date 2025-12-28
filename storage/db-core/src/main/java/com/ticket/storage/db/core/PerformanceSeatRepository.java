package com.ticket.storage.db.core;

import com.ticket.core.enums.PerformanceSeatState;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.*;

import java.util.Collection;
import java.util.List;

public interface PerformanceSeatRepository extends JpaRepository<PerformanceSeatEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({
            @QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")
    })
    List<PerformanceSeatEntity> findByPerformanceIdAndSeatIdInAndState(Long performanceId, List<Long> seatIds, PerformanceSeatState state);

    @Modifying
    @Query("""
            UPDATE PerformanceSeatEntity ps
            SET ps.state = :changeState
            WHERE ps.state = :curState
            AND ps.performanceId = :performanceId
            AND ps.seatId IN :seatIds
    """)
    int updateState(Long performanceId, List<Long> seatIds, PerformanceSeatState curState, PerformanceSeatState changeState);

    @Modifying
    @Query("""
            UPDATE PerformanceSeatEntity ps
            SET ps.state = :changeState
            WHERE ps.id IN :performanceSeatIds
            AND ps.state = :curState
            """)
    int changeHoldStateToAvailable(List<Long> performanceSeatIds, PerformanceSeatState curState, PerformanceSeatState changeState);

    List<PerformanceSeatEntity> findAllByPerformanceIdAndSeatIdIn(Long performanceId, Collection<Long> seatIds);
}
