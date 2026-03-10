package com.ticket.core.domain.performanceseat.usecase;

import com.ticket.core.domain.order.Order;
import com.ticket.core.domain.order.OrderRepository;
import com.ticket.core.domain.order.OrderSeat;
import com.ticket.core.domain.order.OrderSeatRepository;
import com.ticket.core.domain.performance.Performance;
import com.ticket.core.domain.performance.PerformanceFinder;
import com.ticket.core.domain.performanceseat.*;
import com.ticket.core.domain.performanceseat.SeatStatusMessage.SeatAction;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 좌석 선점(Hold) UseCase.
 * DB AVAILABLE 상태의 좌석을 Redis Hold로 전이시키고,
 * PENDING 상태의 주문(Order)을 동시에 생성합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HoldSeatUseCase {

    private final PerformanceFinder performanceFinder;
    private final PerformanceSeatFinder performanceSeatFinder;
    private final SeatSelectionService seatSelectionService;
    private final SeatHoldService seatHoldService;
    private final SeatEventPublisher seatEventPublisher;
    private final OrderRepository orderRepository;
    private final OrderSeatRepository orderSeatRepository;

    public record Input(Long performanceId, List<Long> seatIds, Long memberId) {}

    public record Output(Long orderId, String orderNo, BigDecimal totalAmount, List<SeatInfo> seats) {
        public record SeatInfo(Long performanceSeatId, BigDecimal price) {}
    }

    @Transactional
    public Output execute(final Input input) {
        final Performance performance = performanceFinder.findById(input.performanceId());

        // 1. 예매 오픈 시간 검증
        if (!performance.isBookingOpen(LocalDateTime.now())) {
            throw new CoreException(ErrorType.NOT_YET_RESERVE_TIME);
        }

        // 2. 최대 선점 가능 좌석 수 검증
        if (performance.isOverCount(input.seatIds().size())) {
            throw new CoreException(ErrorType.EXCEED_AVAILABLE_SEATS);
        }

        // 3. DB에서 AVAILABLE 상태 확인 (비관적 락)
        final List<PerformanceSeat> availableSeats = performanceSeatFinder
                .findAvailablePerformanceSeats(input.seatIds(), input.performanceId());
        if (availableSeats.size() != input.seatIds().size()) {
            throw new CoreException(ErrorType.SEAT_COUNT_MISMATCH);
        }

        // 4. Redis Hold (Lua 스크립트 원자적 처리)
        seatHoldService.hold(
                input.performanceId(),
                input.seatIds(),
                input.memberId(),
                performance.getHoldTime()
        );

        // 5. Select 키가 존재하면 정리 (Select 없이 Hold한 경우 무시)
        for (final Long seatId : input.seatIds()) {
            seatSelectionService.forceDeselectIfExists(input.performanceId(), seatId);
        }

        // 6. Order(PENDING) 생성
        final BigDecimal totalAmount = availableSeats.stream()
                .map(PerformanceSeat::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        final Order order = Order.create(input.memberId(), input.performanceId(), totalAmount);
        orderRepository.save(order);

        // 7. OrderSeat 별도 저장
        final List<OrderSeat> orderSeats = availableSeats.stream()
                .map(seat -> OrderSeat.create(order, seat.getId(), seat.getPrice()))
                .toList();
        orderSeatRepository.saveAll(orderSeats);

        // 8. WebSocket HELD 이벤트 발행
        for (final Long seatId : input.seatIds()) {
            seatEventPublisher.publish(SeatStatusMessage.of(input.performanceId(), seatId, SeatAction.HELD));
        }

        log.info("좌석 선점 + 주문 생성 완료: performanceId={}, seatIds={}, memberId={}, orderId={}, orderNo={}",
                input.performanceId(), input.seatIds(), input.memberId(), order.getId(), order.getOrderNo());

        final List<Output.SeatInfo> seatInfos = availableSeats.stream()
                .map(s -> new Output.SeatInfo(s.getId(), s.getPrice()))
                .toList();
        return new Output(order.getId(), order.getOrderNo(), totalAmount, seatInfos);
    }
}

