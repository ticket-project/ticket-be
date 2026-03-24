package com.ticket.core.domain.order.cancel;

import com.ticket.core.domain.member.MemberFinder;
import com.ticket.core.domain.order.event.OrderCancelledEvent;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NonAsciiCharacters")
class CancelOrderUseCaseTest {

    @Mock
    private MemberFinder memberFinder;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderTerminationContextLoader contextLoader;

    @Mock
    private OrderCanceler orderCanceler;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private CancelOrderUseCase useCase;

    private OrderTerminationContext context;

    @BeforeEach
    void setUp() {
        context = new OrderTerminationContext(List.of(), List.of(42L));
    }

    @Test
    void 취소_요청이면_회원과_주문을_검증하고_취소_이벤트를_발행한다() {
        Order order = createOrder(10L, 100L, "hold-key");
        OrderTerminationResult result = new OrderTerminationResult(100L, "hold-key", List.of(42L));
        when(orderRepository.findByOrderKeyAndMemberIdForUpdate("order-key", 1L)).thenReturn(java.util.Optional.of(order));
        when(contextLoader.load(order)).thenReturn(context);
        when(orderCanceler.cancel(eq(order), eq(context), any(LocalDateTime.class))).thenReturn(result);

        useCase.execute(new CancelOrderUseCase.Input("order-key", 1L));

        verify(memberFinder).findActiveMemberById(1L);
        verify(orderRepository).findByOrderKeyAndMemberIdForUpdate("order-key", 1L);
        verify(contextLoader).load(order);
        verify(orderCanceler).cancel(eq(order), eq(context), any(LocalDateTime.class));

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue()).isInstanceOf(OrderCancelledEvent.class);
        OrderCancelledEvent event = (OrderCancelledEvent) eventCaptor.getValue();
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
