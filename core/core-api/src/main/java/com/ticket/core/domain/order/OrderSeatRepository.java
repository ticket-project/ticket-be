package com.ticket.core.domain.order;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderSeatRepository extends JpaRepository<OrderSeat, Long> {

    List<OrderSeat> findByOrderId(Long orderId);

    List<OrderSeat> findByPerformanceSeatIdIn(List<Long> performanceSeatIds);
}
