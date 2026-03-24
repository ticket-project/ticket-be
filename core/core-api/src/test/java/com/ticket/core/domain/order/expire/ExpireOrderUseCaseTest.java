package com.ticket.core.domain.order.expire;

import com.ticket.core.domain.order.event.OrderExpiredEvent;
import com.ticket.core.domain.order.model.Order;
import com.ticket.core.domain.order.repository.OrderRepository;
import com.ticket.core.domain.order.shared.OrderTerminationContext;
import com.ticket.core.domain.order.shared.OrderTerminationContextLoader;
import com.ticket.core.domain.order.shared.OrderTerminationResult;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NonAsciiCharacters")
class ExpireOrderUseCaseTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderTerminationContextLoader contextLoader;

    @Mock
    private OrderExpirer orderExpirer;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private ExpireOrderUseCase useCase;

    private OrderTerminationContext context;

    @BeforeEach
    void setUp() {
        context = new OrderTerminationContext(List.of(), List.of(42L));
    }

    @Test
    void orderId로_조회한_주문이_없으면_noop이다() {
        when(orderRepository.findByIdAndStatusForUpdate(10L, com.ticket.core.enums.OrderState.PENDING)).thenReturn(java.util.Optional.empty());

        useCase.expireByOrderId(10L, LocalDateTime.of(2026, 3, 15, 10, 0));

        verify(orderRepository).findByIdAndStatusForUpdate(10L, com.ticket.core.enums.OrderState.PENDING);
        verifyNoInteractions(orderExpirer, applicationEventPublisher);
    }

    @Test
    void orderId로_조회한_주문이_있으면_만료_이벤트를_발행한다() {
        Order order = createOrder(10L, 100L, "hold-key");
        LocalDateTime now = LocalDateTime.of(2026, 3, 15, 10, 0);
        when(orderRepository.findByIdAndStatusForUpdate(10L, com.ticket.core.enums.OrderState.PENDING)).thenReturn(java.util.Optional.of(order));
        when(contextLoader.load(order)).thenReturn(context);
        when(orderExpirer.expire(eq(order), eq(context), eq(now)))
                .thenReturn(Optional.of(new OrderTerminationResult(100L, "hold-key", List.of(42L))));

        useCase.expireByOrderId(10L, now);

        verify(contextLoader).load(order);
        verify(orderExpirer).expire(order, context, now);
        assertExpiredEventPublished();
    }

    @Test
    void holdKey로_조회한_주문이_있으면_만료_이벤트를_발행한다() {
        Order order = createOrder(10L, 100L, "hold-key");
        LocalDateTime now = LocalDateTime.of(2026, 3, 15, 10, 0);
        when(orderRepository.findByHoldKeyAndStatusForUpdate("hold-key", com.ticket.core.enums.OrderState.PENDING)).thenReturn(java.util.Optional.of(order));
        when(contextLoader.load(order)).thenReturn(context);
        when(orderExpirer.expire(eq(order), eq(context), eq(now)))
                .thenReturn(Optional.of(new OrderTerminationResult(100L, "hold-key", List.of(42L))));

        useCase.expireByHoldKey("hold-key", now);

        verify(contextLoader).load(order);
        verify(orderExpirer).expire(order, context, now);
        assertExpiredEventPublished();
    }

    @Test
    void 만료_처리_결과가_비어있으면_이벤트를_발행하지_않는다() {
        Order order = createOrder(10L, 100L, "hold-key");
        LocalDateTime now = LocalDateTime.of(2026, 3, 15, 10, 0);
        when(orderRepository.findByHoldKeyAndStatusForUpdate("hold-key", com.ticket.core.enums.OrderState.PENDING)).thenReturn(java.util.Optional.of(order));
        when(contextLoader.load(order)).thenReturn(context);
        when(orderExpirer.expire(eq(order), eq(context), eq(now))).thenReturn(Optional.empty());

        useCase.expireByHoldKey("hold-key", now);

        verify(contextLoader).load(order);
        verify(orderExpirer).expire(order, context, now);
        verifyNoInteractions(applicationEventPublisher);
    }

    private void assertExpiredEventPublished() {
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue()).isInstanceOf(OrderExpiredEvent.class);
        OrderExpiredEvent event = (OrderExpiredEvent) eventCaptor.getValue();
        assertThat(event.performanceId()).isEqualTo(100L);
        assertThat(event.holdKey()).isEqualTo("hold-key");
        assertThat(event.seatIds()).containsExactly(42L);
    }

    private Order createOrder(final Long id, final Long performanceId, final String holdKey) {
        Order order = new Order(1L, performanceId, "order-key", holdKey, BigDecimal.TEN, LocalDateTime.now().plusMinutes(5));
        ReflectionTestUtils.setField(order, "id", id);
        return order;
    }
}
