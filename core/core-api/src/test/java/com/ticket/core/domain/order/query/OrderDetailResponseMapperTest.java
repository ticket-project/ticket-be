package com.ticket.core.domain.order.query;

import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.vo.Email;
import com.ticket.core.domain.member.vo.EncodedPassword;
import com.ticket.core.domain.order.model.Order;
import com.ticket.core.domain.order.model.OrderSeat;
import com.ticket.core.domain.performance.Performance;
import com.ticket.core.domain.performanceseat.model.PerformanceSeat;
import com.ticket.core.domain.seat.Seat;
import com.ticket.core.domain.show.Show;
import com.ticket.core.domain.show.meta.SaleType;
import com.ticket.core.domain.show.venue.Venue;
import com.ticket.core.enums.OrderState;
import com.ticket.core.enums.Role;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SuppressWarnings("NonAsciiCharacters")
class OrderDetailResponseMapperTest {

    @Test
    void 공연좌석목록이_비어있으면_예외를_던진다() {
        Order order = createOrder();

        assertThatThrownBy(() -> OrderDetailResponseMapper.toResponse(order, List.of(), List.of(), createMember()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("performanceSeats");
    }

    @Test
    void 대기중인_주문은_남은시간과_좌석정보를_매핑한다() throws Exception {
        Order order = createOrder();
        setField(order, "status", OrderState.PENDING);
        setField(order, "expiresAt", LocalDateTime.now().plusSeconds(120));
        setField(order, "orderKey", "order-key");

        OrderSeat orderSeat = createOrderSeat();
        PerformanceSeat performanceSeat = createPerformanceSeat();
        Member member = createMember();

        var response = OrderDetailResponseMapper.toResponse(
                order,
                List.of(orderSeat),
                List.of(performanceSeat),
                member
        );

        assertThat(response.orderKey()).isEqualTo("order-key");
        assertThat(response.remainingSeconds()).isBetween(0L, 120L);
        assertThat(response.tickets().count()).isEqualTo(1);
        assertThat(response.tickets().seats()).hasSize(1);
        assertThat(response.tickets().seats().getFirst().label()).contains("1F").contains("A").contains("3열").contains("5번");
    }

    private Order createOrder() {
        return new Order(
                1L,
                10L,
                "order-key",
                "hold-key",
                BigDecimal.valueOf(15000),
                LocalDateTime.now().plusMinutes(5)
        );
    }

    private OrderSeat createOrderSeat() throws Exception {
        OrderSeat orderSeat = new OrderSeat(createOrder(), 100L, 200L, BigDecimal.valueOf(15000));
        setField(orderSeat, "id", 1L);
        return orderSeat;
    }

    private PerformanceSeat createPerformanceSeat() throws Exception {
        Show show = new Show(
                "공연",
                "부제",
                "소개",
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 31),
                0L,
                SaleType.GENERAL,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                "image",
                createVenue(),
                null,
                120
        );
        setField(show, "id", 11L);

        Performance performance = new Performance(
                show,
                1L,
                LocalDateTime.of(2026, 3, 15, 19, 30),
                LocalDateTime.of(2026, 3, 15, 22, 0),
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                4,
                300
        );
        setField(performance, "id", 21L);

        Seat seat = new Seat("A", "3", "5", 1, 0, 0);
        setField(seat, "id", 200L);

        PerformanceSeat performanceSeat = new PerformanceSeat(performance, seat, com.ticket.core.enums.PerformanceSeatState.AVAILABLE, BigDecimal.valueOf(15000));
        setField(performanceSeat, "id", 100L);
        return performanceSeat;
    }

    private Venue createVenue() throws Exception {
        Constructor<Venue> constructor = Venue.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Venue venue = constructor.newInstance();
        setField(venue, "id", 31L);
        setField(venue, "name", "공연장");
        return venue;
    }

    private Member createMember() throws Exception {
        Member member = new Member(Email.create("user@example.com"), EncodedPassword.create("encoded"), "사용자", Role.MEMBER);
        setField(member, "id", 1L);
        return member;
    }

    private void setField(final Object target, final String fieldName, final Object value) throws Exception {
        Field field = findField(target.getClass(), fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private Field findField(final Class<?> type, final String fieldName) throws NoSuchFieldException {
        Class<?> current = type;
        while (current != null) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }
}
