package com.ticket.core.domain.order;

import com.ticket.core.api.controller.response.OrderDetailResponse;
import com.ticket.core.domain.hold.HoldHistory;
import com.ticket.core.domain.hold.HoldHistoryRepository;
import com.ticket.core.domain.hold.HoldRedisService;
import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.MemberService;
import com.ticket.core.domain.performanceseat.PerformanceSeat;
import com.ticket.core.domain.performanceseat.PerformanceSeatRepository;
import com.ticket.core.domain.performanceseat.SeatEventPublisher;
import com.ticket.core.domain.performanceseat.SeatStatusMessage;
import com.ticket.core.enums.HoldReleaseReason;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.ticket.core.domain.performanceseat.SeatStatusMessage.SeatAction.RELEASED;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderFinder orderFinder;
    private final OrderRepository orderRepository;
    private final OrderSeatRepository orderSeatRepository;
    private final HoldHistoryRepository holdHistoryRepository;
    private final HoldRedisService holdRedisService;
    private final PerformanceSeatRepository performanceSeatRepository;
    private final MemberService memberService;
    private final SeatEventPublisher seatEventPublisher;

    @Transactional
    public OrderDetailResponse getOrderDetail(final Long orderId, final Long memberId) {
        final Order order = orderFinder.findOwnedById(orderId, memberId);
        if (order.isPending() && order.isExpired(LocalDateTime.now())) {
            expireLoadedOrder(order, LocalDateTime.now());
        }
        final List<OrderSeat> orderSeats = loadOrderSeats(order);
        final List<PerformanceSeat> performanceSeats = loadPerformanceSeats(orderSeats);
        final Member member = memberService.findById(memberId);
        return OrderDetailResponseMapper.toResponse(order, orderSeats, performanceSeats, member);
    }

    @Transactional
    public Long createPendingOrder(
            final Long memberId,
            final Long performanceId,
            final String holdToken,
            final LocalDateTime expiresAt,
            final List<PerformanceSeat> performanceSeats
    ) {
        final BigDecimal totalAmount = performanceSeats.stream()
                .map(PerformanceSeat::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        final Order order = orderRepository.save(
                new Order(memberId, performanceId, holdToken, totalAmount, expiresAt)
        );

        final List<OrderSeat> orderSeats = performanceSeats.stream()
                .map(seat -> new OrderSeat(order, seat.getId(), seat.getSeat().getId(), seat.getPrice()))
                .toList();
        orderSeatRepository.saveAll(orderSeats);

        final List<HoldHistory> holdHistories = performanceSeats.stream()
                .map(seat -> new HoldHistory(
                        holdToken,
                        memberId,
                        performanceId,
                        seat.getId(),
                        seat.getSeat().getId(),
                        expiresAt
                ))
                .toList();
        holdHistoryRepository.saveAll(holdHistories);

        return order.getId();
    }

    @Transactional
    public void cancelOrder(final Long orderId, final Long memberId) {
        final Order order = orderFinder.findPendingOwnedById(orderId, memberId);
        final List<OrderSeat> orderSeats = loadOrderSeats(order);
        final List<HoldHistory> holdHistories = loadHoldHistories(order);

        order.cancel(LocalDateTime.now());
        orderSeats.forEach(OrderSeat::cancel);
        holdHistories.forEach(history -> history.cancel(LocalDateTime.now(), HoldReleaseReason.USER_CANCELED));

        holdRedisService.releaseHold(order.getHoldToken());
        publishSeatEvents(order.getPerformanceId(), orderSeats, RELEASED);
    }

    @Transactional
    public void expireOrder(final Long orderId, final LocalDateTime now) {
        final Order order = orderRepository.findById(orderId)
                .orElse(null);
        if (order == null || !order.isPending()) {
            return;
        }

        expireLoadedOrder(order, now);
    }

    private void expireLoadedOrder(final Order order, final LocalDateTime now) {
        final List<OrderSeat> orderSeats = loadOrderSeats(order);
        final List<HoldHistory> holdHistories = loadHoldHistories(order);

        order.expire(now);
        orderSeats.forEach(OrderSeat::expire);
        holdHistories.forEach(history -> history.expire(now, HoldReleaseReason.TTL_EXPIRED));

        holdRedisService.releaseHold(order.getPerformanceId(), order.getHoldToken(), orderSeats.stream().map(OrderSeat::getSeatId).toList());
        publishSeatEvents(order.getPerformanceId(), orderSeats, RELEASED);
    }

    private List<OrderSeat> loadOrderSeats(final Order order) {
        return orderSeatRepository.findAllByOrder_IdOrderByIdAsc(order.getId());
    }

    private List<HoldHistory> loadHoldHistories(final Order order) {
        return holdHistoryRepository.findAllByHoldTokenOrderByIdAsc(order.getHoldToken());
    }

    private List<PerformanceSeat> loadPerformanceSeats(final List<OrderSeat> orderSeats) {
        final List<Long> performanceSeatIds = orderSeats.stream()
                .map(OrderSeat::getPerformanceSeatId)
                .toList();
        final Map<Long, PerformanceSeat> performanceSeatMap = performanceSeatRepository.findAllById(performanceSeatIds).stream()
                .collect(Collectors.toMap(PerformanceSeat::getId, Function.identity()));

        if (performanceSeatMap.size() != performanceSeatIds.size()) {
            throw new CoreException(ErrorType.NOT_FOUND_DATA, "주문 좌석 상세 정보를 찾을 수 없습니다.");
        }

        return performanceSeatIds.stream()
                .map(performanceSeatMap::get)
                .toList();
    }

    private void publishSeatEvents(
            final Long performanceId,
            final List<OrderSeat> orderSeats,
            final SeatStatusMessage.SeatAction action
    ) {
        for (final OrderSeat orderSeat : orderSeats) {
            seatEventPublisher.publish(SeatStatusMessage.of(performanceId, orderSeat.getSeatId(), action));
        }
    }
}
