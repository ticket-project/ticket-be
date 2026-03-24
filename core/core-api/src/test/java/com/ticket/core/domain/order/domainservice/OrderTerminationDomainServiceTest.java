package com.ticket.core.domain.order.domainservice;

import com.ticket.core.domain.hold.application.HoldHistoryRecorder;
import com.ticket.core.domain.order.finder.OrderSeatFinder;
import com.ticket.core.domain.order.model.Order;
import com.ticket.core.domain.order.model.OrderSeat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NonAsciiCharacters")
class OrderTerminationDomainServiceTest {

    @Mock
    private OrderSeatFinder orderSeatFinder;

    @Mock
    private HoldHistoryRecorder holdHistoryRecorder;

    @Mock
    private OrderLifecycleDomainService orderLifecycleDomainService;

    @InjectMocks
    private OrderTerminationDomainService orderTerminationDomainService;

    @Test
    void 취소시_좌석을_조회하고_취소이력을_기록한다() {
        //given
        Order order = createOrder(10L, 100L, "hold-key");
        OrderSeat orderSeat = new OrderSeat(order, 501L, 42L, BigDecimal.TEN);
        LocalDateTime now = LocalDateTime.of(2026, 3, 15, 10, 0);

        when(orderSeatFinder.getOrderSeatsByOrderId(10L)).thenReturn(List.of(orderSeat));

        //when
        OrderTerminationDomainService.OrderTerminationResult result =
                orderTerminationDomainService.cancel(order, now);

        //then
        assertThat(result.performanceId()).isEqualTo(100L);
        assertThat(result.holdKey()).isEqualTo("hold-key");
        assertThat(result.seatIds()).containsExactly(42L);
        verify(orderLifecycleDomainService).cancel(order, List.of(orderSeat), now);
        verify(holdHistoryRecorder).recordCanceled(
                1L,
                100L,
                "hold-key",
                now,
                List.of(orderSeat)
        );
    }

    @Test
    void 상태가_pending이_아니면_만료결과는_비어있다() {
        //given
        Order order = createOrder(10L, 100L, "hold-key");
        order.cancel(LocalDateTime.of(2026, 3, 15, 9, 0));

        //when
        java.util.Optional<OrderTerminationDomainService.OrderTerminationResult> result =
                orderTerminationDomainService.expire(order, LocalDateTime.of(2026, 3, 15, 10, 0));

        //then
        assertThat(result).isEmpty();
        verifyNoInteractions(orderSeatFinder, holdHistoryRecorder, orderLifecycleDomainService);
    }

    @Test
    void 만료시_좌석을_조회하고_만료이력을_기록한다() {
        //given
        Order order = createOrder(10L, 100L, "hold-key");
        OrderSeat orderSeat = new OrderSeat(order, 501L, 42L, BigDecimal.TEN);
        LocalDateTime now = LocalDateTime.of(2026, 3, 15, 10, 0);
        when(orderSeatFinder.getOrderSeatsByOrderId(10L)).thenReturn(List.of(orderSeat));

        //when
        java.util.Optional<OrderTerminationDomainService.OrderTerminationResult> result =
                orderTerminationDomainService.expire(order, now);

        //then
        assertThat(result).isPresent();
        verify(orderLifecycleDomainService).expire(order, List.of(orderSeat), now);
        verify(holdHistoryRecorder).recordExpired(
                1L,
                100L,
                "hold-key",
                now,
                List.of(orderSeat)
        );
    }

    private Order createOrder(final Long id, final Long performanceId, final String holdKey) {
        Order order = new Order(
                1L,
                performanceId,
                "order-key",
                holdKey,
                BigDecimal.TEN,
                LocalDateTime.now().plusMinutes(5)
        );
        ReflectionTestUtils.setField(order, "id", id);
        return order;
    }
}
