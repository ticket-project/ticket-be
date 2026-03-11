package com.ticket.core.domain.order.repository;

import com.ticket.core.domain.order.model.OrderSeat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderSeatRepository extends JpaRepository<OrderSeat, Long> {

    List<OrderSeat> findAllByOrder_IdOrderByIdAsc(Long orderId);
}
