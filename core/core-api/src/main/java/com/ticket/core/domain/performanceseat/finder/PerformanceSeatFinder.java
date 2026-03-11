package com.ticket.core.domain.performanceseat.finder;

import com.ticket.core.domain.order.model.OrderSeat;
import com.ticket.core.domain.performanceseat.model.PerformanceSeat;
import com.ticket.core.domain.performanceseat.repository.PerformanceSeatRepository;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PerformanceSeatFinder {

    private final PerformanceSeatRepository performanceSeatRepository;

    public List<PerformanceSeat> findByPerformanceIdAndSeatIdIn(final Long performanceId, final List<Long> seatIds) {
        return performanceSeatRepository.findAllByPerformanceIdAndSeatIdIn(performanceId, seatIds);
    }

    public List<PerformanceSeat> findAllByOrderSeats(final List<OrderSeat> orderSeats) {
        final List<Long> performanceSeatIds = orderSeats.stream()
                .map(OrderSeat::getPerformanceSeatId)
                .toList();
        final Map<Long, PerformanceSeat> performanceSeatMap = performanceSeatRepository.findAllById(performanceSeatIds).stream()
                .collect(Collectors.toMap(PerformanceSeat::getId, Function.identity()));

        if (performanceSeatMap.size() != performanceSeatIds.size()) {
            throw new CoreException(ErrorType.NOT_FOUND_DATA, "주문 좌석 상세 정보를 찾을 수 없습니다.");
        }

        return performanceSeatIds.stream()
                .map(performanceSeatMap::get)
                .toList();
    }

}
