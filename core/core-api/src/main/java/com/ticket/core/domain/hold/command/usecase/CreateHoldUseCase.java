package com.ticket.core.domain.hold.command.usecase;

import com.ticket.core.domain.hold.event.HoldCreatedEvent;
import com.ticket.core.domain.hold.model.HoldSnapshot;
import com.ticket.core.domain.hold.support.HoldRedisService;
import com.ticket.core.domain.hold.support.HoldSeatAvailabilityValidator;
import com.ticket.core.domain.order.application.CreateOrderApplicationService;
import com.ticket.core.domain.order.model.Order;
import com.ticket.core.domain.order.repository.OrderRepository;
import com.ticket.core.domain.performance.Performance;
import com.ticket.core.domain.performance.PerformanceFinder;
import com.ticket.core.domain.performanceseat.model.PerformanceSeat;
import com.ticket.core.enums.OrderState;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CreateHoldUseCase {

    private static final long ORDER_LOCK_WAIT_SECONDS = 2L;
    private static final long ORDER_LOCK_LEASE_SECONDS = 5L;
    private static final String PENDING_ORDER_LOCK_KEY = "lock:pending-order:%d:%d";

    private final PerformanceFinder performanceFinder;
    private final HoldSeatAvailabilityValidator holdSeatAvailabilityValidator;
    private final HoldRedisService holdRedisService;
    private final CreateOrderApplicationService createOrderApplicationService;
    private final OrderRepository orderRepository;
    private final RedissonClient redissonClient;
    private final ApplicationEventPublisher applicationEventPublisher;

    public record Input(Long performanceId, List<Long> seatIds, Long memberId) {}
    public record Output(String orderKey) {}

    @Transactional
    public Output execute(final Input input) {
        final List<Long> seatIds = normalizeSeatIds(input.seatIds());
        final Performance performance = performanceFinder.findValidPerformanceById(input.performanceId());
        validateSeatCount(performance, seatIds);

        final RLock orderLock = redissonClient.getLock(orderLockKey(input.memberId(), input.performanceId()));
        try {
            final boolean locked = orderLock.tryLock(ORDER_LOCK_WAIT_SECONDS, ORDER_LOCK_LEASE_SECONDS, TimeUnit.SECONDS);
            if (!locked) {
                throw new CoreException(ErrorType.HOLD_BUSY, "주문 시작 처리 중입니다. 잠시 후 다시 시도해주세요.");
            }

            ensureNoPendingOrder(input.memberId(), input.performanceId());

            final List<PerformanceSeat> performanceSeats = holdSeatAvailabilityValidator.validate(input.performanceId(), seatIds);
            final HoldSnapshot snapshot = holdRedisService.createHold(
                    input.memberId(),
                    input.performanceId(),
                    seatIds,
                    Duration.ofSeconds(performance.getHoldTime())
            );
            applicationEventPublisher.publishEvent(new HoldCreatedEvent(snapshot));

            final Order order = createOrderApplicationService.createPendingOrder(
                    input.memberId(),
                    input.performanceId(),
                    snapshot.holdToken(),
                    snapshot.expiresAt(),
                    performanceSeats
            );

            return new Output(order.getOrderKey());
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CoreException(ErrorType.HOLD_PROCESSING_FAILED, "주문 시작 처리 중 인터럽트가 발생했습니다.");
        } finally {
            unlockQuietly(orderLock);
        }
    }

    private List<Long> normalizeSeatIds(final List<Long> requestedSeatIds) {
        if (requestedSeatIds.size() != new HashSet<>(requestedSeatIds).size()) {
            throw new CoreException(ErrorType.INVALID_REQUEST, "중복된 seatId가 포함되어 있습니다.");
        }

        final List<Long> seatIds = requestedSeatIds.stream().sorted().toList();
        if (seatIds.isEmpty()) {
            throw new CoreException(ErrorType.INVALID_REQUEST, "선점할 좌석이 없습니다.");
        }
        return seatIds;
    }

    private void validateSeatCount(final Performance performance, final List<Long> seatIds) {
        if (performance.isOverCount(seatIds.size())) {
            throw new CoreException(ErrorType.EXCEED_HOLD_LIMIT);
        }
    }

    private void ensureNoPendingOrder(final Long memberId, final Long performanceId) {
        final boolean alreadyExists = orderRepository.findByMemberIdAndPerformanceIdAndStatus(memberId, performanceId, OrderState.PENDING)
                .isPresent();
        if (alreadyExists) {
            throw new CoreException(ErrorType.PENDING_ORDER_ALREADY_EXISTS);
        }
    }

    private String orderLockKey(final Long memberId, final Long performanceId) {
        return String.format(PENDING_ORDER_LOCK_KEY, memberId, performanceId);
    }

    private void unlockQuietly(final RLock lock) {
        if (lock != null && lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}
