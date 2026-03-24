package com.ticket.core.domain.order.domainservice;

import com.ticket.core.domain.hold.application.HoldHistoryRecorder;
import com.ticket.core.domain.hold.model.HoldSnapshot;
import com.ticket.core.domain.hold.support.HoldManager;
import com.ticket.core.domain.hold.support.HoldSeatAvailabilityValidator;
import com.ticket.core.domain.order.application.CreateOrderApplicationService;
import com.ticket.core.domain.order.model.Order;
import com.ticket.core.domain.performanceseat.model.PerformanceSeat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NonAsciiCharacters")
class OrderStartDomainServiceTest {

    @Mock
    private HoldSeatAvailabilityValidator holdSeatAvailabilityValidator;

    @Mock
    private HoldManager holdManager;

    @Mock
    private HoldHistoryRecorder holdHistoryRecorder;

    @Mock
    private CreateOrderApplicationService createOrderApplicationService;

    @InjectMocks
    private OrderStartDomainService orderStartDomainService;

    @Test
    void 홀드와_주문을_생성하고_created_이력을_남긴다() {
        //given
        Duration holdDuration = Duration.ofSeconds(600);
        List<Long> seatIds = List.of(3L, 7L);
        List<PerformanceSeat> performanceSeats = List.of(
                org.mockito.Mockito.mock(PerformanceSeat.class),
                org.mockito.Mockito.mock(PerformanceSeat.class)
        );
        HoldSnapshot snapshot = new HoldSnapshot(
                "hold-key",
                20L,
                10L,
                seatIds,
                LocalDateTime.of(2026, 3, 15, 12, 0)
        );
        Order order = new Order(
                20L,
                10L,
                "order-key",
                "hold-key",
                BigDecimal.valueOf(120000),
                snapshot.expiresAt()
        );

        when(holdSeatAvailabilityValidator.validate(10L, seatIds)).thenReturn(performanceSeats);
        when(holdManager.createHold(20L, 10L, seatIds, holdDuration)).thenReturn(snapshot);
        when(createOrderApplicationService.createPendingOrder(
                20L,
                10L,
                "hold-key",
                snapshot.expiresAt(),
                performanceSeats
        )).thenReturn(order);

        //when
        OrderStartDomainService.OrderResult result =
                orderStartDomainService.start(20L, 10L, seatIds, holdDuration);

        //then
        assertThat(result.orderKey()).isEqualTo("order-key");
        assertThat(result.snapshot()).isEqualTo(snapshot);
        verify(holdHistoryRecorder).recordCreated(
                20L,
                10L,
                "hold-key",
                snapshot.expiresAt().minus(holdDuration),
                snapshot.expiresAt(),
                performanceSeats
        );
    }

    @Test
    void 주문생성이_실패하면_hold를_보상해제한다() {
        Duration holdDuration = Duration.ofSeconds(600);
        List<Long> seatIds = List.of(3L, 7L);
        List<PerformanceSeat> performanceSeats = List.of(org.mockito.Mockito.mock(PerformanceSeat.class));
        HoldSnapshot snapshot = new HoldSnapshot(
                "hold-key",
                20L,
                10L,
                seatIds,
                LocalDateTime.of(2026, 3, 15, 12, 0)
        );

        when(holdSeatAvailabilityValidator.validate(10L, seatIds)).thenReturn(performanceSeats);
        when(holdManager.createHold(20L, 10L, seatIds, holdDuration)).thenReturn(snapshot);
        when(createOrderApplicationService.createPendingOrder(
                20L,
                10L,
                "hold-key",
                snapshot.expiresAt(),
                performanceSeats
        )).thenThrow(new RuntimeException("order failed"));

        assertThatThrownBy(() -> orderStartDomainService.start(20L, 10L, seatIds, holdDuration))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("order failed");

        verify(holdManager).release(10L, "hold-key", seatIds);
        verify(holdHistoryRecorder, never()).recordCreated(
                20L,
                10L,
                "hold-key",
                snapshot.expiresAt().minus(holdDuration),
                snapshot.expiresAt(),
                performanceSeats
        );
    }

    @Test
    void 이력기록이_실패하면_hold를_보상해제한다() {
        Duration holdDuration = Duration.ofSeconds(600);
        List<Long> seatIds = List.of(3L, 7L);
        List<PerformanceSeat> performanceSeats = List.of(org.mockito.Mockito.mock(PerformanceSeat.class));
        HoldSnapshot snapshot = new HoldSnapshot(
                "hold-key",
                20L,
                10L,
                seatIds,
                LocalDateTime.of(2026, 3, 15, 12, 0)
        );
        Order order = new Order(
                20L,
                10L,
                "order-key",
                "hold-key",
                BigDecimal.valueOf(120000),
                snapshot.expiresAt()
        );

        when(holdSeatAvailabilityValidator.validate(10L, seatIds)).thenReturn(performanceSeats);
        when(holdManager.createHold(20L, 10L, seatIds, holdDuration)).thenReturn(snapshot);
        when(createOrderApplicationService.createPendingOrder(
                20L,
                10L,
                "hold-key",
                snapshot.expiresAt(),
                performanceSeats
        )).thenReturn(order);
        doThrow(new RuntimeException("history failed"))
                .when(holdHistoryRecorder).recordCreated(
                        20L,
                        10L,
                        "hold-key",
                        snapshot.expiresAt().minus(holdDuration),
                        snapshot.expiresAt(),
                        performanceSeats
                );

        assertThatThrownBy(() -> orderStartDomainService.start(20L, 10L, seatIds, holdDuration))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("history failed");

        verify(holdManager).release(10L, "hold-key", seatIds);
    }
}
