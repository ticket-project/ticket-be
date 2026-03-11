package com.ticket.core.domain.order.repository;

import com.ticket.core.domain.order.model.Order;
import com.ticket.core.enums.OrderState;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByIdAndMemberId(Long id, Long memberId);

    Optional<Order> findByMemberIdAndPerformanceIdAndStatus(Long memberId, Long performanceId, OrderState status);

    Optional<Order> findByHoldToken(String holdToken);

    Slice<Order> findAllByStatusAndExpiresAtBefore(OrderState status, LocalDateTime expiresAt, Pageable pageable);
}
