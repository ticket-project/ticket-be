package com.ticket.core.domain.performanceseat.query;

import com.ticket.core.domain.order.model.OrderSeat;
import com.ticket.core.domain.performanceseat.model.PerformanceSeat;
import com.ticket.core.domain.performanceseat.repository.PerformanceSeatRepository;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PerformanceSeatFinder {

    private final PerformanceSeatRepository performanceSeatRepository;

    public List<PerformanceSeat> findAllByOrderSeats(final List<OrderSeat> orderSeats) {
        final List<Long> performanceSeatIds = orderSeats.stream()
                .map(OrderSeat::getPerformanceSeatId)
                .toList();
        final List<PerformanceSeat> performanceSeats = performanceSeatRepository.findAllById(performanceSeatIds);
        if (performanceSeats.size() != performanceSeatIds.size()) {
            throw new CoreException(ErrorType.NOT_FOUND_DATA, "주문 좌석과 매핑되는 회차 좌석을 모두 찾을 수 없습니다.");
        }
        return performanceSeats;
    }

}
