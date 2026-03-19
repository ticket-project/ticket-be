package com.ticket.core.domain.queue.runtime;

import com.ticket.core.domain.queue.model.QueueEntryStatus;
import com.ticket.core.support.exception.AuthException;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SuppressWarnings("NonAsciiCharacters")
class QueueEntryRuntimeTest {

    @Test
    void 본인_공연의_엔트리인지_판단한다() {
        QueueEntryRuntime entry = createWaitingEntry();

        assertThat(entry.isOwnedBy(10L, 100L)).isTrue();
        assertThat(entry.isOwnedBy(10L, 101L)).isFalse();
        assertThat(entry.isOwnedBy(11L, 100L)).isFalse();
    }

    @Test
    void 다른_회원이면_권한_예외를_던진다() {
        QueueEntryRuntime entry = createWaitingEntry();

        assertThatThrownBy(() -> entry.assertOwnedBy(10L, 999L))
                .isInstanceOf(AuthException.class);
    }

    @Test
    void 대기_엔트리는_WAITING_상태다() {
        QueueEntryRuntime waiting = createWaitingEntry();

        assertThat(waiting.isWaiting()).isTrue();
        assertThat(waiting.isAdmitted()).isFalse();
        assertThat(waiting.hasQueueToken()).isFalse();
    }

    @Test
    void 입장_엔트리는_ADMITTED_상태와_토큰을_가진다() {
        QueueEntryRuntime admitted = new QueueEntryRuntime(
                10L,
                100L,
                "qe-1",
                QueueEntryStatus.ADMITTED,
                1L,
                "qt-1",
                LocalDateTime.of(2026, 3, 17, 10, 0)
        );

        assertThat(admitted.isWaiting()).isFalse();
        assertThat(admitted.isAdmitted()).isTrue();
        assertThat(admitted.hasQueueToken()).isTrue();
    }

    @Test
    void 입장_상태는_토큰과_만료시간이_반드시_있어야_한다() {
        assertThatThrownBy(() -> new QueueEntryRuntime(
                10L,
                100L,
                "qe-1",
                QueueEntryStatus.ADMITTED,
                1L,
                null,
                LocalDateTime.of(2026, 3, 17, 10, 0)
        )).isInstanceOf(CoreException.class)
                .satisfies(thrown -> assertThat(((CoreException) thrown).getErrorType()).isEqualTo(ErrorType.INVALID_REQUEST));

        assertThatThrownBy(() -> new QueueEntryRuntime(
                10L,
                100L,
                "qe-1",
                QueueEntryStatus.ADMITTED,
                1L,
                "qt-1",
                null
        )).isInstanceOf(CoreException.class)
                .satisfies(thrown -> assertThat(((CoreException) thrown).getErrorType()).isEqualTo(ErrorType.INVALID_REQUEST));
    }

    private QueueEntryRuntime createWaitingEntry() {
        return new QueueEntryRuntime(10L, 100L, "qe-1", QueueEntryStatus.WAITING, 1L, null, null);
    }
}
