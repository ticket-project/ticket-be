package com.ticket.core.domain.order.repository;

import com.ticket.core.domain.order.model.Order;
import com.ticket.core.enums.OrderState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByIdAndMemberId(Long id, Long memberId);

    Optional<Order> findByMemberIdAndPerformanceIdAndStatus(Long memberId, Long performanceId, OrderState status);

    Optional<Order> findByHoldToken(String holdToken);

    List<Order> findAllByStatusAndExpiresAtBefore(OrderState status, LocalDateTime expiresAt);
}
