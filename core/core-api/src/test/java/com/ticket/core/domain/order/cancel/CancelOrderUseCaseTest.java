package com.ticket.core.domain.order.cancel;

import com.ticket.core.domain.member.MemberFinder;
import com.ticket.core.domain.order.model.Order;
import com.ticket.core.domain.order.model.OrderSeat;
import com.ticket.core.domain.order.release.HoldReleaseOutboxWriter;
import com.ticket.core.domain.order.repository.OrderRepository;
import com.ticket.core.domain.order.repository.OrderSeatRepository;
import com.ticket.core.domain.order.shared.OrderTerminationResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
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
    private OrderSeatRepository orderSeatRepository;

    @Mock
    private OrderCanceler orderCanceler;

    @Mock
    private HoldReleaseOutboxWriter holdReleaseOutboxWriter;

    @InjectMocks
    private CancelOrderUseCase useCase;

    @Test
    void 취소_요청이면_회원과_주문을_검증하고_hold_release_outbox를_적재한다() {
        final Order order = createOrder(10L, 100L, "hold-key");
        final OrderSeat orderSeat = mock(OrderSeat.class);
        final OrderTerminationResult result = new OrderTerminationResult(100L, "hold-key", List.of(42L));
        when(orderRepository.findByOrderKeyAndMemberIdForUpdate("order-key", 1L)).thenReturn(java.util.Optional.of(order));
        when(orderSeatRepository.findAllByOrder_IdOrderByIdAsc(10L)).thenReturn(List.of(orderSeat));
        when(orderCanceler.cancel(eq(order), eq(List.of(orderSeat)), any(LocalDateTime.class))).thenReturn(result);

        useCase.execute(new CancelOrderUseCase.Input("order-key", 1L));

        verify(memberFinder).findActiveMemberById(1L);
        verify(orderRepository).findByOrderKeyAndMemberIdForUpdate("order-key", 1L);
        verify(orderSeatRepository).findAllByOrder_IdOrderByIdAsc(10L);
        verify(orderCanceler).cancel(eq(order), eq(List.of(orderSeat)), any(LocalDateTime.class));
        verify(holdReleaseOutboxWriter).append(result);
    }

    private Order createOrder(final Long id, final Long performanceId, final String holdKey) {
        final Order order = new Order(1L, performanceId, "order-key", holdKey, BigDecimal.TEN, LocalDateTime.now().plusMinutes(5));
        ReflectionTestUtils.setField(order, "id", id);
        return order;
    }
}
