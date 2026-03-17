package com.ticket.core.domain.queue.runtime;

import com.ticket.core.domain.queue.model.QueueEntryStatus;
import com.ticket.core.support.exception.AuthException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SuppressWarnings("NonAsciiCharacters")
class QueueEntryRuntimeTest {

    @Test
    void 본인_공연의_엔트리인지_판단한다() {
        //given
        QueueEntryRuntime entry = createWaitingEntry();

        //when //then
        assertThat(entry.isOwnedBy(10L, 100L)).isTrue();
        assertThat(entry.isOwnedBy(10L, 101L)).isFalse();
        assertThat(entry.isOwnedBy(11L, 100L)).isFalse();
    }

    @Test
    void 다른_회원이_접근하면_예외를_던진다() {
        //given
        QueueEntryRuntime entry = createWaitingEntry();

        //when //then
        assertThatThrownBy(() -> entry.assertOwnedBy(10L, 999L))
                .isInstanceOf(AuthException.class);
    }

    @Test
    void 재진입시_상태에_따라_정리_계획을_반환한다() {
        //given
        QueueEntryRuntime waiting = createWaitingEntry();
        QueueEntryRuntime admitted = new QueueEntryRuntime(
                10L,
                100L,
                "qe-1",
                QueueEntryStatus.ADMITTED,
                1L,
                "qt-1",
                LocalDateTime.of(2026, 3, 17, 10, 0)
        );

        //when //then
        assertThat(waiting.planReentry(10L, 100L).type()).isEqualTo(QueueEntryActionType.LEAVE_WAITING);
        assertThat(admitted.planReentry(10L, 100L).type()).isEqualTo(QueueEntryActionType.LEAVE_ADMITTED_AND_ADVANCE);
    }

    @Test
    void 이탈시_상태에_따라_정리_계획을_반환한다() {
        //given
        QueueEntryRuntime waiting = createWaitingEntry();
        QueueEntryRuntime admitted = new QueueEntryRuntime(
                10L,
                100L,
                "qe-1",
                QueueEntryStatus.ADMITTED,
                1L,
                "qt-1",
                LocalDateTime.of(2026, 3, 17, 10, 0)
        );

        //when //then
        assertThat(waiting.planLeave(10L, 100L).type()).isEqualTo(QueueEntryActionType.LEAVE_WAITING);
        assertThat(admitted.planLeave(10L, 100L).type()).isEqualTo(QueueEntryActionType.LEAVE_ADMITTED_AND_ADVANCE);
    }

    private QueueEntryRuntime createWaitingEntry() {
        return new QueueEntryRuntime(10L, 100L, "qe-1", QueueEntryStatus.WAITING, 1L, null, null);
    }
}
