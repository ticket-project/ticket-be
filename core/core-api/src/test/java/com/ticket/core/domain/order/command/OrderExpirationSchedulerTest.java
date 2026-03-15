package com.ticket.core.domain.order.command;

import com.ticket.core.domain.order.command.usecase.TerminateOrderUseCase;
import com.ticket.core.domain.order.model.Order;
import com.ticket.core.domain.order.repository.OrderRepository;
import com.ticket.core.enums.OrderState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
class OrderExpirationSchedulerTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private TerminateOrderUseCase terminateOrderUseCase;

    @InjectMocks
    private OrderExpirationScheduler orderExpirationScheduler;

    @Test
    void 만료대상_주문이_없으면_종료한다() {
        when(orderRepository.findAllByStatusAndExpiresAtBefore(eq(OrderState.PENDING), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(new SliceImpl<>(List.of()));

        orderExpirationScheduler.expirePendingOrders();

        verify(terminateOrderUseCase, times(0)).expireByOrderId(any(), any());
    }

    @Test
    void 만료대상_주문이_있으면_순서대로_만료처리한다() {
        Order first = createOrder(1L, null);
        Order second = createOrder(2L, null);
        Slice<Order> slice = new SliceImpl<>(List.of(first, second));
        when(orderRepository.findAllByStatusAndExpiresAtBefore(eq(OrderState.PENDING), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(slice);

        orderExpirationScheduler.expirePendingOrders();

        verify(terminateOrderUseCase).expireByOrderId(eq(1L), any(LocalDateTime.class));
        verify(terminateOrderUseCase).expireByOrderId(eq(2L), any(LocalDateTime.class));
        verify(orderRepository, times(1)).findAllByStatusAndExpiresAtBefore(eq(OrderState.PENDING), any(LocalDateTime.class), any(Pageable.class));
    }

    @Test
    void 주문_만료처리중_예외가_나도_배치를_중단하고_반환한다() {
        Order order = createOrder(1L, "order-1");
        Slice<Order> slice = new SliceImpl<>(List.of(order));
        when(orderRepository.findAllByStatusAndExpiresAtBefore(eq(OrderState.PENDING), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(slice);
        doThrow(new RuntimeException("boom")).when(terminateOrderUseCase).expireByOrderId(eq(1L), any(LocalDateTime.class));

        orderExpirationScheduler.expirePendingOrders();

        verify(terminateOrderUseCase).expireByOrderId(eq(1L), any(LocalDateTime.class));
        verify(orderRepository, times(1)).findAllByStatusAndExpiresAtBefore(eq(OrderState.PENDING), any(LocalDateTime.class), any(Pageable.class));
    }

    private Order createOrder(final Long id, final String orderKey) {
        Order order = mock(Order.class);
        when(order.getId()).thenReturn(id);
        if (orderKey != null) {
            when(order.getOrderKey()).thenReturn(orderKey);
        }
        return order;
    }
}
