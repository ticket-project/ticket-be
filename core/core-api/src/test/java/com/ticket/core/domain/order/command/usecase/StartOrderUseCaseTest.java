package com.ticket.core.domain.order.command.usecase;

import com.ticket.core.domain.hold.application.HoldHistoryRecorder;
import com.ticket.core.domain.hold.event.HoldCreatedEvent;
import com.ticket.core.domain.hold.model.HoldSnapshot;
import com.ticket.core.domain.hold.support.HoldManager;
import com.ticket.core.domain.hold.support.HoldSeatAvailabilityValidator;
import com.ticket.core.domain.member.MemberFinder;
import com.ticket.core.domain.order.application.CreateOrderApplicationService;
import com.ticket.core.domain.order.model.Order;
import com.ticket.core.domain.order.repository.OrderRepository;
import com.ticket.core.domain.performance.Performance;
import com.ticket.core.domain.performance.PerformanceFinder;
import com.ticket.core.domain.performanceseat.model.PerformanceSeat;
import com.ticket.core.enums.OrderState;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NonAsciiCharacters")
class StartOrderUseCaseTest {

    @Mock
    private MemberFinder memberFinder;

    @Mock
    private PerformanceFinder performanceFinder;

    @Mock
    private HoldSeatAvailabilityValidator holdSeatAvailabilityValidator;

    @Mock
    private HoldManager holdManager;

    @Mock
    private HoldHistoryRecorder holdHistoryRecorder;

    @Mock
    private CreateOrderApplicationService createOrderApplicationService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private StartOrderUseCase startOrderUseCase;

    @Test
    void 중복된_좌석_ID가_있으면_예외를_던진다() {
        StartOrderUseCase.Input input = new StartOrderUseCase.Input(10L, List.of(3L, 1L, 3L), 20L);

        assertThatThrownBy(() -> startOrderUseCase.execute(input))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> assertThat(((CoreException) exception).getErrorType()).isEqualTo(ErrorType.INVALID_REQUEST));

        verifyNoInteractions(memberFinder, performanceFinder, holdSeatAvailabilityValidator, holdManager);
    }

    @Test
    void 좌석_ID가_비어있으면_예외를_던진다() {
        StartOrderUseCase.Input input = new StartOrderUseCase.Input(10L, List.of(), 20L);

        assertThatThrownBy(() -> startOrderUseCase.execute(input))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> assertThat(((CoreException) exception).getErrorType()).isEqualTo(ErrorType.INVALID_REQUEST));

        verifyNoInteractions(memberFinder, performanceFinder, holdSeatAvailabilityValidator, holdManager);
    }

    @Test
    void 최대_선점_가능_수량을_초과하면_예외를_던진다() {
        StartOrderUseCase.Input input = new StartOrderUseCase.Input(10L, List.of(1L, 2L, 3L), 20L);
        Performance performance = createPerformance(2, 300);

        when(performanceFinder.findValidPerformanceById(10L)).thenReturn(performance);

        assertThatThrownBy(() -> startOrderUseCase.execute(input))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> assertThat(((CoreException) exception).getErrorType()).isEqualTo(ErrorType.EXCEED_HOLD_LIMIT));

        verify(memberFinder).findActiveMemberById(20L);
        verify(performanceFinder).findValidPerformanceById(10L);
        verifyNoInteractions(holdSeatAvailabilityValidator, holdManager, createOrderApplicationService);
    }

    @Test
    void 보류중인_주문이_이미_존재하면_예외를_던진다() {
        StartOrderUseCase.Input input = new StartOrderUseCase.Input(10L, List.of(2L, 1L), 20L);
        Performance performance = createPerformance(3, 300);

        when(performanceFinder.findValidPerformanceById(10L)).thenReturn(performance);
        when(orderRepository.findByMemberIdAndPerformanceIdAndStatus(20L, 10L, OrderState.PENDING))
                .thenReturn(Optional.of(mock(Order.class)));

        assertThatThrownBy(() -> startOrderUseCase.execute(input))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> assertThat(((CoreException) exception).getErrorType()).isEqualTo(ErrorType.PENDING_ORDER_ALREADY_EXISTS));

        verify(memberFinder).findActiveMemberById(20L);
        verify(performanceFinder).findValidPerformanceById(10L);
        verify(orderRepository).findByMemberIdAndPerformanceIdAndStatus(20L, 10L, OrderState.PENDING);
        verifyNoInteractions(holdSeatAvailabilityValidator, holdManager, createOrderApplicationService);
    }

    @Test
    void 유효한_요청이면_보류와_주문을_생성한다() {
        StartOrderUseCase.Input input = new StartOrderUseCase.Input(10L, List.of(7L, 3L), 20L);
        Performance performance = createPerformance(5, 420);
        List<Long> normalizedSeatIds = List.of(3L, 7L);
        List<PerformanceSeat> performanceSeats = List.of(mock(PerformanceSeat.class), mock(PerformanceSeat.class));
        HoldSnapshot snapshot = new HoldSnapshot(
                "hold-key",
                20L,
                10L,
                normalizedSeatIds,
                LocalDateTime.of(2026, 3, 15, 12, 0)
        );
        Order order = new Order(20L, 10L, "order-key", "hold-key", BigDecimal.valueOf(120000), snapshot.expiresAt());

        when(performanceFinder.findValidPerformanceById(10L)).thenReturn(performance);
        when(orderRepository.findByMemberIdAndPerformanceIdAndStatus(20L, 10L, OrderState.PENDING))
                .thenReturn(Optional.empty());
        when(holdSeatAvailabilityValidator.validate(10L, normalizedSeatIds)).thenReturn(performanceSeats);
        when(holdManager.createHold(20L, 10L, normalizedSeatIds, java.time.Duration.ofSeconds(420)))
                .thenReturn(snapshot);
        when(createOrderApplicationService.createPendingOrder(20L, 10L, "hold-key", snapshot.expiresAt(), performanceSeats))
                .thenReturn(order);

        StartOrderUseCase.Output output = startOrderUseCase.execute(input);

        assertThat(output.orderKey()).isEqualTo("order-key");
        verify(memberFinder).findActiveMemberById(20L);
        verify(performanceFinder).findValidPerformanceById(10L);
        verify(orderRepository).findByMemberIdAndPerformanceIdAndStatus(20L, 10L, OrderState.PENDING);
        verify(holdSeatAvailabilityValidator).validate(10L, normalizedSeatIds);
        verify(holdManager).createHold(20L, 10L, normalizedSeatIds, java.time.Duration.ofSeconds(420));
        verify(applicationEventPublisher).publishEvent(any(HoldCreatedEvent.class));
        verify(createOrderApplicationService).createPendingOrder(20L, 10L, "hold-key", snapshot.expiresAt(), performanceSeats);
        verify(holdHistoryRecorder).recordActiveHold(20L, 10L, "hold-key", snapshot.expiresAt(), performanceSeats);
        verify(applicationEventPublisher, never()).publishEvent(eq("hold-key"));

        var inOrder = inOrder(holdManager, applicationEventPublisher, createOrderApplicationService, holdHistoryRecorder);
        inOrder.verify(holdManager).createHold(20L, 10L, normalizedSeatIds, java.time.Duration.ofSeconds(420));
        inOrder.verify(applicationEventPublisher).publishEvent(any(HoldCreatedEvent.class));
        inOrder.verify(createOrderApplicationService).createPendingOrder(20L, 10L, "hold-key", snapshot.expiresAt(), performanceSeats);
        inOrder.verify(holdHistoryRecorder).recordActiveHold(20L, 10L, "hold-key", snapshot.expiresAt(), performanceSeats);
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
