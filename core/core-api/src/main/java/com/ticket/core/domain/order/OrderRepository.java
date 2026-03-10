package com.ticket.core.domain.order;

import com.ticket.core.enums.OrderState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNo(String orderNo);

    List<Order> findByPerformanceIdAndStateIn(Long performanceId, List<OrderState> states);
}
