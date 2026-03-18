package com.ticket.core.domain.order.command.usecase;

import com.ticket.core.domain.hold.finder.HoldHistoryFinder;
import com.ticket.core.domain.hold.model.HoldHistory;
import com.ticket.core.domain.member.MemberFinder;
import com.ticket.core.domain.order.domainservice.OrderTerminationDomainService;
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
@SuppressWarnings("NonAsciiCharacters")
class TerminateOrderUseCaseTest {

    @Mock
    private MemberFinder memberFinder;
    @Mock
    private OrderFinder orderFinder;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderTerminationDomainService orderTerminationDomainService;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private TerminateOrderUseCase useCase;

    @Test
    void 취소시_주문상태를_전이하고_이벤트를_발행한다() {
        //given
        Order order = createOrder(10L, 100L, "hold-key");
        when(orderFinder.findPendingOwnedByOrderKeyForUpdate("order-key", 1L)).thenReturn(order);
        when(orderTerminationDomainService.cancel(org.mockito.Mockito.eq(order), org.mockito.ArgumentMatchers.any(LocalDateTime.class)))
                .thenReturn(new OrderTerminationDomainService.OrderTerminationResult(100L, "hold-key", List.of(42L)));

        //when
        useCase.cancel(new TerminateOrderUseCase.Input("order-key", 1L));

        //then
        verify(memberFinder).findActiveMemberById(1L);
        verify(orderTerminationDomainService).cancel(org.mockito.Mockito.eq(order), org.mockito.ArgumentMatchers.any(LocalDateTime.class));
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().toString()).contains("hold-key").contains("42");
    }

    @Test
    void 만료대상_주문이_없으면_noop이다() {
        //given
        when(orderRepository.findByIdAndStatusForUpdate(10L, OrderState.PENDING)).thenReturn(Optional.empty());

        //when
        useCase.expireByOrderId(10L, LocalDateTime.now());

        //then
        verify(orderRepository).findByIdAndStatusForUpdate(10L, OrderState.PENDING);
        verifyNoInteractions(orderTerminationDomainService, applicationEventPublisher);
    }

    @Test
    void holdKey로_조회한_주문이_pending이면_만료처리한다() {
        //given
        Order order = createOrder(10L, 100L, "hold-key");
        LocalDateTime now = LocalDateTime.of(2026, 3, 15, 10, 0);

        when(orderRepository.findByHoldKeyAndStatusForUpdate("hold-key", OrderState.PENDING)).thenReturn(Optional.of(order));
        when(orderTerminationDomainService.expire(order, now))
                .thenReturn(Optional.of(new OrderTerminationDomainService.OrderTerminationResult(100L, "hold-key", List.of(42L))));

        //when
        useCase.expireByHoldKey("hold-key", now);

        //then
        verify(orderTerminationDomainService).expire(order, now);
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().toString()).contains("hold-key").contains("42");
    }

    @Test
    void 상태가_pending이_아닌_주문이면_만료처리를_건너뛴다() {
        //given
        Order order = createOrder(10L, 100L, "hold-key");
        order.cancel(LocalDateTime.of(2026, 3, 15, 9, 0));
        when(orderRepository.findByHoldKeyAndStatusForUpdate("hold-key", OrderState.PENDING)).thenReturn(Optional.of(order));
        when(orderTerminationDomainService.expire(order, LocalDateTime.of(2026, 3, 15, 10, 0))).thenReturn(Optional.empty());

        //when
        useCase.expireByHoldKey("hold-key", LocalDateTime.of(2026, 3, 15, 10, 0));

        //then
        verify(orderTerminationDomainService).expire(order, LocalDateTime.of(2026, 3, 15, 10, 0));
        verifyNoInteractions(applicationEventPublisher);
    }

    private Order createOrder(final Long id, final Long performanceId, final String holdKey) {
        Order order = new Order(1L, performanceId, "order-key", holdKey, BigDecimal.TEN, LocalDateTime.now().plusMinutes(5));
        ReflectionTestUtils.setField(order, "id", id);
        return order;
    }
}

