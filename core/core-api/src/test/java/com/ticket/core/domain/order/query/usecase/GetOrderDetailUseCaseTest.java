package com.ticket.core.domain.order.query.usecase;

import com.ticket.core.api.controller.response.OrderDetailResponse;
import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.MemberFinder;
import com.ticket.core.domain.member.vo.Email;
import com.ticket.core.domain.order.finder.OrderFinder;
import com.ticket.core.domain.order.finder.OrderSeatFinder;
import com.ticket.core.domain.order.model.Order;
import com.ticket.core.domain.order.model.OrderSeat;
import com.ticket.core.domain.performance.Performance;
import com.ticket.core.domain.performanceseat.finder.PerformanceSeatFinder;
import com.ticket.core.domain.performanceseat.model.PerformanceSeat;
import com.ticket.core.domain.seat.Seat;
import com.ticket.core.domain.show.Show;
import com.ticket.core.domain.show.meta.SaleType;
import com.ticket.core.domain.show.venue.Venue;
import com.ticket.core.enums.PerformanceSeatState;
import com.ticket.core.enums.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NonAsciiCharacters")
class GetOrderDetailUseCaseTest {

    @Mock
    private OrderFinder orderFinder;
    @Mock
    private OrderSeatFinder orderSeatFinder;
    @Mock
    private PerformanceSeatFinder performanceSeatFinder;
    @Mock
    private MemberFinder memberFinder;

    @InjectMocks
    private GetOrderDetailUseCase useCase;

    @Test
    void 주문_상세를_응답으로_매핑한다() throws Exception {
        //given
        Venue venue = createVenue("올림픽홀");
        Show show = createShow(100L, "뮤지컬", venue);
        Performance performance = createPerformance(10L, show);
        Seat seat = new Seat("A", "10", "7", 1, 10.0, 20.0);
        ReflectionTestUtils.setField(seat, "id", 42L);
        PerformanceSeat performanceSeat = new PerformanceSeat(performance, seat, PerformanceSeatState.AVAILABLE, BigDecimal.valueOf(120000));
        ReflectionTestUtils.setField(performanceSeat, "id", 501L);

        Order order = new Order(1L, 10L, "order-key", "hold-key", BigDecimal.valueOf(120000), LocalDateTime.now().plusMinutes(10));
        ReflectionTestUtils.setField(order, "id", 77L);
        OrderSeat orderSeat = new OrderSeat(order, 501L, 42L, BigDecimal.valueOf(120000));

        Member member = Member.createSocialMember(Email.create("user@example.com"), "홍길동", Role.MEMBER);
        ReflectionTestUtils.setField(member, "id", 1L);

        when(orderFinder.findOwnedByOrderKey("order-key", 1L)).thenReturn(order);
        when(orderSeatFinder.getOrderSeatsByOrderId(77L)).thenReturn(List.of(orderSeat));
        when(performanceSeatFinder.findAllByOrderSeats(List.of(orderSeat))).thenReturn(List.of(performanceSeat));
        when(memberFinder.findActiveMemberById(1L)).thenReturn(member);

        GetOrderDetailUseCase.Output output = useCase.execute(new GetOrderDetailUseCase.Input("order-key", 1L));

        //when
        OrderDetailResponse response = output.order();
        //then
        assertThat(response.orderKey()).isEqualTo("order-key");
        assertThat(response.show().title()).isEqualTo("뮤지컬");
        assertThat(response.performance().venueName()).isEqualTo("올림픽홀");
        assertThat(response.booker().email()).isEqualTo("user@example.com");
        assertThat(response.tickets().count()).isEqualTo(1);
    }

    private Venue createVenue(final String name) throws Exception {
        java.lang.reflect.Constructor<Venue> constructor = Venue.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Venue venue = constructor.newInstance();
        ReflectionTestUtils.setField(venue, "id", 1L);
        ReflectionTestUtils.setField(venue, "name", name);
        ReflectionTestUtils.setField(venue, "viewBoxWidth", 1000);
        ReflectionTestUtils.setField(venue, "viewBoxHeight", 800);
        ReflectionTestUtils.setField(venue, "seatDiameter", 10.0);
        return venue;
    }

    private Show createShow(final Long id, final String title, final Venue venue) {
        Show show = new Show(title, title + " 부제", "info", LocalDate.now(), LocalDate.now().plusDays(10), 10L,
                SaleType.GENERAL, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(10), "image", venue, null, 120);
        ReflectionTestUtils.setField(show, "id", id);
        return show;
    }

    private Performance createPerformance(final Long id, final Show show) {
        Performance performance = new Performance(show, 1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2),
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1), 2, 300);
        ReflectionTestUtils.setField(performance, "id", id);
        return performance;
    }
}

