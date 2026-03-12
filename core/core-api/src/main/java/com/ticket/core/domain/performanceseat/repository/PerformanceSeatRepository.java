package com.ticket.core.domain.performanceseat.repository;

import com.ticket.core.domain.performanceseat.model.PerformanceSeat;
import com.ticket.core.enums.PerformanceSeatState;
import org.springframework.data.jpa.repository.*;

import java.util.Collection;
import java.util.List;

public interface PerformanceSeatRepository extends JpaRepository<PerformanceSeat, Long> {

    List<PerformanceSeat> findAllByPerformanceIdAndSeatIdIn(Long performanceId, Collection<Long> seatIds);

    List<PerformanceSeat> findAllByStateEquals(PerformanceSeatState state);

}
