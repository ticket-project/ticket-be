package com.ticket.storage.db.core;

import com.ticket.core.enums.PerformanceSeatState;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.*;

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
            SET ps.state = :performanceSeatState
            WHERE ps.state = 'AVAILABLE'
            AND ps.performanceId = :performanceId
            AND ps.seatId IN :seatIds
    """)
    int updateState(Long performanceId, List<Long> seatIds, PerformanceSeatState performanceSeatState);

    @Modifying
    @Query("""
            UPDATE PerformanceSeatEntity ps
            SET ps.state = 'AVAILABLE'
            WHERE ps.id IN :performanceSeatIds
            """)
    void changeStateToAvailable(List<Long> performanceSeatIds);
}
