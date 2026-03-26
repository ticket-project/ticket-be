package com.ticket.core.domain.queue.command;

import com.ticket.core.domain.queue.model.QueueEntryStatus;
import com.ticket.core.domain.queue.model.QueueLevel;
import com.ticket.core.domain.queue.runtime.QueueTicket;
import com.ticket.core.domain.queue.runtime.QueueTicketStore;
import com.ticket.core.domain.queue.support.QueuePolicy;
import com.ticket.core.domain.queue.support.QueuePolicyResolver;
import com.ticket.core.domain.queue.model.QueueEntryId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
class QueueAdmissionAdvancerTest {

    @Mock
    private QueuePolicyResolver queuePolicyResolver;

    @Mock
    private QueueTicketStore queueTicketStore;

    private QueueAdmissionAdvancer queueAdmissionAdvancer;
    private final Clock fixedClock = Clock.fixed(Instant.parse("2026-03-15T10:00:00Z"), ZoneId.of("Asia/Seoul"));

    @BeforeEach
    void setUp() {
        queueAdmissionAdvancer = new QueueAdmissionAdvancer(queuePolicyResolver, queueTicketStore, fixedClock);
    }

    @Test
    void 빈자리가_있고_대기자가_있으면_다음_대기자를_입장시킨다() {
        final QueuePolicy policy = createPolicy(1);
        final QueueTicket admitted = createAdmitted(10L, "qe-100", "qt-100");

        when(queuePolicyResolver.resolve(10L)).thenReturn(policy);
        when(queueTicketStore.countActive(10L)).thenReturn(0L, 1L);
        when(queueTicketStore.admitNextWaiting(10L, Duration.ofMinutes(10), Duration.ofHours(1), LocalDateTime.of(2026, 3, 15, 19, 0)))
                .thenReturn(Optional.of(admitted));

        queueAdmissionAdvancer.advance(10L);

        verify(queueTicketStore).admitNextWaiting(10L, Duration.ofMinutes(10), Duration.ofHours(1), LocalDateTime.of(2026, 3, 15, 19, 0));
    }

    @Test
    void 이미_active가_가득찼으면_대기자를_입장시키지_않는다() {
        when(queuePolicyResolver.resolve(10L)).thenReturn(createPolicy(1));
        when(queueTicketStore.countActive(10L)).thenReturn(1L);

        queueAdmissionAdvancer.advance(10L);

        verify(queueTicketStore, never()).admitNextWaiting(10L, Duration.ofMinutes(10), Duration.ofHours(1), LocalDateTime.of(2026, 3, 15, 19, 0));
    }

    @Test
    void 대기자가_없으면_즉시_종료한다() {
        when(queuePolicyResolver.resolve(10L)).thenReturn(createPolicy(1));
        when(queueTicketStore.countActive(10L)).thenReturn(0L);
        when(queueTicketStore.admitNextWaiting(10L, Duration.ofMinutes(10), Duration.ofHours(1), LocalDateTime.of(2026, 3, 15, 19, 0)))
                .thenReturn(Optional.empty());

        queueAdmissionAdvancer.advance(10L);

        verify(queueTicketStore).admitNextWaiting(10L, Duration.ofMinutes(10), Duration.ofHours(1), LocalDateTime.of(2026, 3, 15, 19, 0));
    }

    @Test
    void 토큰_만료를_처리한_후_다음_대기자를_입장시킨다() {
        when(queuePolicyResolver.resolve(10L)).thenReturn(createPolicy(1));
        when(queueTicketStore.countActive(10L)).thenReturn(0L, 1L);
        when(queueTicketStore.admitNextWaiting(10L, Duration.ofMinutes(10), Duration.ofHours(1), LocalDateTime.of(2026, 3, 15, 19, 0)))
                .thenReturn(Optional.of(createAdmitted(10L, "qe-201", "qt-201")));

        queueAdmissionAdvancer.handleTokenExpired(10L, QueueEntryId.from("qe-200"), "qt-200");

        verify(queueTicketStore).expireAdmitted(10L, QueueEntryId.from("qe-200"), "qt-200");
        verify(queueTicketStore).admitNextWaiting(10L, Duration.ofMinutes(10), Duration.ofHours(1), LocalDateTime.of(2026, 3, 15, 19, 0));
    }

    @Test
    void 여러_대기자를_입장시킬_때마다_현재_시각을_다시_계산한다() {
        final QueueAdmissionAdvancer advancer = new QueueAdmissionAdvancer(
                queuePolicyResolver,
                queueTicketStore,
                new SequenceClock(
                        Instant.parse("2026-03-15T10:00:00Z"),
                        Instant.parse("2026-03-15T10:00:01Z")
                )
        );

        when(queuePolicyResolver.resolve(10L)).thenReturn(createPolicy(2));
        when(queueTicketStore.countActive(10L)).thenReturn(0L, 1L, 2L);
        when(queueTicketStore.admitNextWaiting(10L, Duration.ofMinutes(10), Duration.ofHours(1), LocalDateTime.of(2026, 3, 15, 19, 0)))
                .thenReturn(Optional.of(createAdmitted(10L, "qe-100", "qt-100")));
        when(queueTicketStore.admitNextWaiting(10L, Duration.ofMinutes(10), Duration.ofHours(1), LocalDateTime.of(2026, 3, 15, 19, 0, 1)))
                .thenReturn(Optional.of(createAdmitted(10L, "qe-101", "qt-101")));

        advancer.advance(10L);

        final InOrder inOrder = inOrder(queueTicketStore);
        inOrder.verify(queueTicketStore).admitNextWaiting(10L, Duration.ofMinutes(10), Duration.ofHours(1), LocalDateTime.of(2026, 3, 15, 19, 0));
        inOrder.verify(queueTicketStore).admitNextWaiting(10L, Duration.ofMinutes(10), Duration.ofHours(1), LocalDateTime.of(2026, 3, 15, 19, 0, 1));
    }

    private QueuePolicy createPolicy(final int maxActiveUsers) {
        return new QueuePolicy(
                true,
                QueueLevel.LEVEL_1,
                maxActiveUsers,
                Duration.ofMinutes(10),
                Duration.ofHours(1)
        );
    }

    private QueueTicket createAdmitted(final Long performanceId, final String entryId, final String token) {
        return new QueueTicket(
                performanceId,
                100L,
                entryId,
                QueueEntryStatus.ADMITTED,
                null,
                token,
                LocalDateTime.of(2026, 3, 15, 20, 30)
        );
    }

    private static final class SequenceClock extends Clock {

        private final Instant[] instants;
        private int index;

        private SequenceClock(final Instant... instants) {
            this.instants = instants;
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("Asia/Seoul");
        }

        @Override
        public Clock withZone(final ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            final int currentIndex = Math.min(index, instants.length - 1);
            index++;
            return instants[currentIndex];
        }
    }
}
