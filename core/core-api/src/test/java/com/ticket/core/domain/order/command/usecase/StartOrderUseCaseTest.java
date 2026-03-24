package com.ticket.core.domain.order.command.usecase;

import com.ticket.core.domain.hold.event.HoldCreatedEvent;
import com.ticket.core.domain.hold.model.HoldSnapshot;
import com.ticket.core.domain.member.MemberFinder;
import com.ticket.core.domain.order.domainservice.OrderStartDomainService;
import com.ticket.core.domain.order.model.Order;
import com.ticket.core.domain.order.repository.OrderRepository;
import com.ticket.core.domain.performance.Performance;
import com.ticket.core.domain.performance.PerformanceFinder;
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
    private OrderRepository orderRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private OrderStartDomainService orderStartDomainService;

    @InjectMocks
    private StartOrderUseCase startOrderUseCase;

    @Test
    void 중복된_좌석_ID가_있으면_예외를_던진다() {
        //given
        StartOrderUseCase.Input input = new StartOrderUseCase.Input(10L, List.of(3L, 1L, 3L), 20L);

        //when
        //then
        assertThatThrownBy(() -> startOrderUseCase.execute(input))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> assertThat(((CoreException) exception).getErrorType()).isEqualTo(ErrorType.INVALID_REQUEST));

        verifyNoInteractions(memberFinder, performanceFinder, orderStartDomainService);
    }

    @Test
    void 좌석_ID가_비어있으면_예외를_던진다() {
        //given
        StartOrderUseCase.Input input = new StartOrderUseCase.Input(10L, List.of(), 20L);

        //when
        //then
        assertThatThrownBy(() -> startOrderUseCase.execute(input))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> assertThat(((CoreException) exception).getErrorType()).isEqualTo(ErrorType.INVALID_REQUEST));

        verifyNoInteractions(memberFinder, performanceFinder, orderStartDomainService);
    }

    @Test
    void 최대_선점_가능_수량을_초과하면_예외를_던진다() {
        //given
        StartOrderUseCase.Input input = new StartOrderUseCase.Input(10L, List.of(1L, 2L, 3L), 20L);
        Performance performance = createPerformance(2, 300);

        when(performanceFinder.findValidPerformanceById(10L)).thenReturn(performance);

        //when
        //then
        assertThatThrownBy(() -> startOrderUseCase.execute(input))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> assertThat(((CoreException) exception).getErrorType()).isEqualTo(ErrorType.EXCEED_HOLD_LIMIT));

        verify(memberFinder).findActiveMemberById(20L);
        verify(performanceFinder).findValidPerformanceById(10L);
        verifyNoInteractions(orderStartDomainService);
    }

    @Test
    void 보류중인_주문이_이미_존재하면_예외를_던진다() {
        //given
        StartOrderUseCase.Input input = new StartOrderUseCase.Input(10L, List.of(2L, 1L), 20L);
        Performance performance = createPerformance(3, 300);

        when(performanceFinder.findValidPerformanceById(10L)).thenReturn(performance);
        when(orderRepository.findByMemberIdAndPerformanceIdAndStatus(20L, 10L, OrderState.PENDING))
                .thenReturn(Optional.of(mock(Order.class)));

        //when
        //then
        assertThatThrownBy(() -> startOrderUseCase.execute(input))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> assertThat(((CoreException) exception).getErrorType()).isEqualTo(ErrorType.PENDING_ORDER_ALREADY_EXISTS));

        verify(memberFinder).findActiveMemberById(20L);
        verify(performanceFinder).findValidPerformanceById(10L);
        verify(orderRepository).findByMemberIdAndPerformanceIdAndStatus(20L, 10L, OrderState.PENDING);
        verifyNoInteractions(orderStartDomainService);
    }

    @Test
    void 유효한_요청이면_보류와_주문을_생성한다() {
        //given
        StartOrderUseCase.Input input = new StartOrderUseCase.Input(10L, List.of(7L, 3L), 20L);
        Performance performance = createPerformance(5, 600);
        List<Long> normalizedSeatIds = List.of(3L, 7L);
        HoldSnapshot snapshot = new HoldSnapshot(
                "hold-key",
                20L,
                10L,
                normalizedSeatIds,
                LocalDateTime.of(2026, 3, 15, 12, 0)
        );

        when(performanceFinder.findValidPerformanceById(10L)).thenReturn(performance);
        when(orderRepository.findByMemberIdAndPerformanceIdAndStatus(20L, 10L, OrderState.PENDING))
                .thenReturn(Optional.empty());
        when(orderStartDomainService.start(20L, 10L, normalizedSeatIds, java.time.Duration.ofSeconds(600)))
                .thenReturn(new OrderStartDomainService.OrderResult("order-key", snapshot));

        //when
        StartOrderUseCase.Output output = startOrderUseCase.execute(input);

        //then
        assertThat(output.orderKey()).isEqualTo("order-key");
        assertThat(output.status()).isEqualTo(OrderState.PENDING);
        assertThat(output.expiresAt()).isEqualTo(LocalDateTime.of(2026, 3, 15, 12, 0));
        verify(memberFinder).findActiveMemberById(20L);
        verify(performanceFinder).findValidPerformanceById(10L);
        verify(orderRepository).findByMemberIdAndPerformanceIdAndStatus(20L, 10L, OrderState.PENDING);
        verify(orderStartDomainService).start(20L, 10L, normalizedSeatIds, java.time.Duration.ofSeconds(600));
        verify(applicationEventPublisher).publishEvent(any(HoldCreatedEvent.class));
        verify(applicationEventPublisher, never()).publishEvent(eq("hold-key"));

        InOrder inOrder = inOrder(orderStartDomainService, applicationEventPublisher);
        inOrder.verify(orderStartDomainService).start(20L, 10L, normalizedSeatIds, java.time.Duration.ofSeconds(600));
        inOrder.verify(applicationEventPublisher).publishEvent(any(HoldCreatedEvent.class));
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
