package com.ticket.core.domain.order.command.usecase;

import com.ticket.core.domain.hold.finder.HoldHistoryFinder;
import com.ticket.core.domain.hold.model.HoldHistory;
import com.ticket.core.domain.member.MemberFinder;
import com.ticket.core.domain.order.domainservice.OrderLifecycleDomainService;
import com.ticket.core.domain.order.finder.OrderFinder;
import com.ticket.core.domain.order.finder.OrderSeatFinder;
import com.ticket.core.domain.order.model.Order;
import com.ticket.core.domain.order.model.OrderSeat;
import com.ticket.core.domain.order.repository.OrderRepository;
import com.ticket.core.enums.OrderState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TerminateOrderUseCaseTest {

    @Mock
    private MemberFinder memberFinder;
    @Mock
    private OrderFinder orderFinder;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderSeatFinder orderSeatFinder;
    @Mock
    private HoldHistoryFinder holdHistoryFinder;
    @Mock
    private OrderLifecycleDomainService orderLifecycleDomainService;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private TerminateOrderUseCase useCase;

    @Test
    void 취소시_주문상태를_전이하고_이벤트를_발행한다() {
        Order order = createOrder(10L, 100L, "hold-key");
        OrderSeat orderSeat = new OrderSeat(order, 501L, 42L, BigDecimal.TEN);
        HoldHistory holdHistory = mock(HoldHistory.class);
        LocalDateTime now = LocalDateTime.of(2026, 3, 15, 10, 0);

        when(orderFinder.findPendingOwnedByOrderKeyForUpdate("order-key", 1L)).thenReturn(order);
        when(orderSeatFinder.getOrderSeatsByOrderId(10L)).thenReturn(List.of(orderSeat));
        when(holdHistoryFinder.findActiveByHoldKey("hold-key")).thenReturn(List.of(holdHistory));

        useCase.cancel("order-key", 1L, now);

        verify(memberFinder).findActiveMemberById(1L);
        verify(orderLifecycleDomainService).cancel(order, List.of(orderSeat), List.of(holdHistory), now);
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().toString()).contains("hold-key").contains("42");
    }

    @Test
    void 만료대상_주문이_없으면_noop이다() {
        when(orderRepository.findByIdAndStatusForUpdate(10L, OrderState.PENDING)).thenReturn(Optional.empty());

        useCase.expireByOrderId(10L, LocalDateTime.now());

        verify(orderRepository).findByIdAndStatusForUpdate(10L, OrderState.PENDING);
        verifyNoInteractions(orderLifecycleDomainService, applicationEventPublisher);
    }

    @Test
    void holdKey로_조회한_주문이_pending이면_만료처리한다() {
        Order order = createOrder(10L, 100L, "hold-key");
        OrderSeat orderSeat = new OrderSeat(order, 501L, 42L, BigDecimal.TEN);
        HoldHistory holdHistory = mock(HoldHistory.class);
        LocalDateTime now = LocalDateTime.of(2026, 3, 15, 10, 0);

        when(orderRepository.findByHoldKeyAndStatusForUpdate("hold-key", OrderState.PENDING)).thenReturn(Optional.of(order));
        when(orderSeatFinder.getOrderSeatsByOrderId(10L)).thenReturn(List.of(orderSeat));
        when(holdHistoryFinder.findActiveByHoldKey("hold-key")).thenReturn(List.of(holdHistory));

        useCase.expireByHoldKey("hold-key", now);

        verify(orderLifecycleDomainService).expire(order, List.of(orderSeat), List.of(holdHistory), now);
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().toString()).contains("hold-key").contains("42");
    }

    @Test
    void 상태가_pending이_아닌_주문이면_만료처리를_건너뛴다() {
        Order order = createOrder(10L, 100L, "hold-key");
        order.cancel(LocalDateTime.of(2026, 3, 15, 9, 0));
        when(orderRepository.findByHoldKeyAndStatusForUpdate("hold-key", OrderState.PENDING)).thenReturn(Optional.of(order));

        useCase.expireByHoldKey("hold-key", LocalDateTime.of(2026, 3, 15, 10, 0));

        verifyNoInteractions(orderSeatFinder, holdHistoryFinder, orderLifecycleDomainService, applicationEventPublisher);
    }

    private Order createOrder(final Long id, final Long performanceId, final String holdKey) {
        Order order = new Order(1L, performanceId, "order-key", holdKey, BigDecimal.TEN, LocalDateTime.now().plusMinutes(5));
        ReflectionTestUtils.setField(order, "id", id);
        return order;
    }
}
