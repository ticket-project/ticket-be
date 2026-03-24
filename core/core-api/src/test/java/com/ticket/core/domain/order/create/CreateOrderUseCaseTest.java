package com.ticket.core.domain.order.create;

import com.ticket.core.domain.hold.application.HoldHistoryRecorder;
import com.ticket.core.domain.hold.event.HoldCreatedEvent;
import com.ticket.core.domain.hold.model.HoldSnapshot;
import com.ticket.core.domain.order.model.Order;
import com.ticket.core.domain.performance.Performance;
import com.ticket.core.domain.performanceseat.model.PerformanceSeat;
import com.ticket.core.enums.OrderState;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NonAsciiCharacters")
class CreateOrderUseCaseTest {

    @Mock
    private CreateOrderValidator preconditionChecker;

    @Mock
    private HoldAllocator holdAllocator;

    @Mock
    private OrderCreator orderCreator;

    @Mock
    private HoldHistoryRecorder holdHistoryRecorder;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private CreateOrderUseCase createOrderUseCase;

    @Test
    void 중복된_좌석_ID가_있으면_예외를_던진다() {
        CreateOrderUseCase.Input input = new CreateOrderUseCase.Input(10L, List.of(3L, 1L, 3L), 20L);

        assertThatThrownBy(() -> createOrderUseCase.execute(input))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> assertThat(((CoreException) exception).getErrorType()).isEqualTo(ErrorType.INVALID_REQUEST));

        verifyNoInteractions(preconditionChecker, holdAllocator, orderCreator);
    }

    @Test
    void 좌석_ID가_비어있으면_예외를_던진다() {
        CreateOrderUseCase.Input input = new CreateOrderUseCase.Input(10L, List.of(), 20L);

        assertThatThrownBy(() -> createOrderUseCase.execute(input))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> assertThat(((CoreException) exception).getErrorType()).isEqualTo(ErrorType.INVALID_REQUEST));

        verifyNoInteractions(preconditionChecker, holdAllocator, orderCreator);
    }

    @Test
    void 유효한_요청이면_hold와_주문을_생성한다() {
        CreateOrderUseCase.Input input = new CreateOrderUseCase.Input(10L, List.of(7L, 3L), 20L);
        RequestedSeatIds seatIds = RequestedSeatIds.from(input.seatIds());
        Performance performance = createPerformance(5, 600);
        List<PerformanceSeat> seats = List.of(mock(PerformanceSeat.class), mock(PerformanceSeat.class));
        HoldSnapshot snapshot = holdSnapshot(seatIds.values());
        HoldAllocation allocation = new HoldAllocation(snapshot, seats);
        Order order = order(snapshot);

        when(preconditionChecker.validate(20L, 10L, seatIds)).thenReturn(performance);
        when(holdAllocator.allocate(20L, 10L, seatIds, Duration.ofSeconds(600))).thenReturn(allocation);
        when(orderCreator.createPendingOrder(20L, 10L, "hold-key", snapshot.expiresAt(), seats))
                .thenReturn(order);

        CreateOrderUseCase.Output output = createOrderUseCase.execute(input);

        assertThat(output.orderKey()).isEqualTo("order-key");
        assertThat(output.status()).isEqualTo(OrderState.PENDING);
        assertThat(output.expiresAt()).isEqualTo(snapshot.expiresAt());
        verify(holdHistoryRecorder).recordCreated(
                20L,
                10L,
                "hold-key",
                snapshot.expiresAt().minusSeconds(600),
                snapshot.expiresAt(),
                seats
        );
        verify(applicationEventPublisher).publishEvent(any(HoldCreatedEvent.class));

        InOrder inOrder = inOrder(preconditionChecker, holdAllocator, orderCreator, holdHistoryRecorder, applicationEventPublisher);
        inOrder.verify(preconditionChecker).validate(20L, 10L, seatIds);
        inOrder.verify(holdAllocator).allocate(20L, 10L, seatIds, Duration.ofSeconds(600));
        inOrder.verify(orderCreator).createPendingOrder(20L, 10L, "hold-key", snapshot.expiresAt(), seats);
        inOrder.verify(holdHistoryRecorder).recordCreated(
                20L,
                10L,
                "hold-key",
                snapshot.expiresAt().minusSeconds(600),
                snapshot.expiresAt(),
                seats
        );
        inOrder.verify(applicationEventPublisher).publishEvent(any(HoldCreatedEvent.class));
    }

    @Test
    void 주문_생성에_실패하면_hold를_해제한다() {
        CreateOrderUseCase.Input input = new CreateOrderUseCase.Input(10L, List.of(7L, 3L), 20L);
        RequestedSeatIds seatIds = RequestedSeatIds.from(input.seatIds());
        Performance performance = createPerformance(5, 600);
        HoldAllocation allocation = new HoldAllocation(holdSnapshot(seatIds.values()), List.of(mock(PerformanceSeat.class)));

        when(preconditionChecker.validate(20L, 10L, seatIds)).thenReturn(performance);
        when(holdAllocator.allocate(20L, 10L, seatIds, Duration.ofSeconds(600))).thenReturn(allocation);
        when(orderCreator.createPendingOrder(
                20L,
                10L,
                "hold-key",
                allocation.snapshot().expiresAt(),
                allocation.performanceSeats()
        )).thenThrow(new RuntimeException("order failed"));

        assertThatThrownBy(() -> createOrderUseCase.execute(input))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("order failed");

        verify(holdAllocator).release(allocation);
    }

    @Test
    void 이벤트_발행에_실패하면_hold를_해제한다() {
        CreateOrderUseCase.Input input = new CreateOrderUseCase.Input(10L, List.of(7L, 3L), 20L);
        RequestedSeatIds seatIds = RequestedSeatIds.from(input.seatIds());
        Performance performance = createPerformance(5, 600);
        List<PerformanceSeat> seats = List.of(mock(PerformanceSeat.class));
        HoldSnapshot snapshot = holdSnapshot(seatIds.values());
        HoldAllocation allocation = new HoldAllocation(snapshot, seats);

        when(preconditionChecker.validate(20L, 10L, seatIds)).thenReturn(performance);
        when(holdAllocator.allocate(20L, 10L, seatIds, Duration.ofSeconds(600))).thenReturn(allocation);
        when(orderCreator.createPendingOrder(20L, 10L, "hold-key", snapshot.expiresAt(), seats))
                .thenReturn(order(snapshot));
        doThrow(new RuntimeException("event failed"))
                .when(applicationEventPublisher).publishEvent(any(HoldCreatedEvent.class));

        assertThatThrownBy(() -> createOrderUseCase.execute(input))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("event failed");

        verify(holdAllocator).release(allocation);
    }

    private HoldSnapshot holdSnapshot(final List<Long> seatIds) {
        return new HoldSnapshot("hold-key", 20L, 10L, seatIds, LocalDateTime.of(2026, 3, 15, 12, 0));
    }

    private Order order(final HoldSnapshot snapshot) {
        return new Order(20L, 10L, "order-key", "hold-key", BigDecimal.valueOf(120000), snapshot.expiresAt());
    }

    private Performance createPerformance(final int maxCanHoldCount, final int holdTimeSeconds) {
        LocalDateTime now = LocalDateTime.of(2026, 3, 15, 10, 0);
        return new Performance(
                null,
                1L,
                now.plusDays(1),
                now.plusDays(1).plusHours(2),
                now.minusHours(1),
                now.plusHours(3),
                maxCanHoldCount,
                holdTimeSeconds
        );
    }
}
