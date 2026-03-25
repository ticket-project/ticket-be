package com.ticket.core.domain.order.release;

import com.ticket.core.domain.order.shared.OrderTerminationResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NonAsciiCharacters")
class HoldReleaseOutboxWriterTest {

    @Mock
    private HoldReleaseOutboxRepository holdReleaseOutboxRepository;

    @Test
    void outbox를_저장하면_생성된_id를_반환한다() {
        final OrderTerminationResult result = new OrderTerminationResult(1L, "hold-key", List.of(10L, 20L));
        final HoldReleaseOutbox saved = HoldReleaseOutbox.create(1L, "hold-key", List.of(10L, 20L), LocalDateTime.now());
        ReflectionTestUtils.setField(saved, "id", 99L);
        when(holdReleaseOutboxRepository.save(any(HoldReleaseOutbox.class))).thenReturn(saved);

        final Long outboxId = writer().append(result);

        assertThat(outboxId).isEqualTo(99L);
    }

    private HoldReleaseOutboxWriter writer() {
        return new HoldReleaseOutboxWriter(holdReleaseOutboxRepository);
    }
}
