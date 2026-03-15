package com.ticket.core.domain.order.finder;

import com.ticket.core.domain.order.model.OrderSeat;
import com.ticket.core.domain.order.repository.OrderSeatRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
class OrderSeatFinderTest {

    @Mock
    private OrderSeatRepository orderSeatRepository;

    @InjectMocks
    private OrderSeatFinder orderSeatFinder;

    @Test
    void 주문아이디로_좌석목록을_조회해_반환한다() {
        OrderSeat first = org.mockito.Mockito.mock(OrderSeat.class);
        OrderSeat second = org.mockito.Mockito.mock(OrderSeat.class);
        when(orderSeatRepository.findAllByOrder_IdOrderByIdAsc(1L)).thenReturn(List.of(first, second));

        List<OrderSeat> result = orderSeatFinder.getOrderSeatsByOrderId(1L);

        assertThat(result).containsExactly(first, second);
        verify(orderSeatRepository).findAllByOrder_IdOrderByIdAsc(1L);
    }

    @Test
    void 주문좌석이_없으면_빈목록을_반환한다() {
        when(orderSeatRepository.findAllByOrder_IdOrderByIdAsc(2L)).thenReturn(List.of());

        List<OrderSeat> result = orderSeatFinder.getOrderSeatsByOrderId(2L);

        assertThat(result).isEmpty();
        verify(orderSeatRepository).findAllByOrder_IdOrderByIdAsc(2L);
    }
}
