package com.ticket.core.domain.queue.shared;

import com.ticket.core.domain.queue.model.QueueEntryId;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SuppressWarnings("NonAsciiCharacters")
class QueueEntryIdTest {

    @Test
    void 앞뒤_공백을_제거한다() {
        final QueueEntryId queueEntryId = QueueEntryId.from("  qe-1  ");

        assertThat(queueEntryId.value()).isEqualTo("qe-1");
    }

    @Test
    void 빈값이면_예외를_던진다() {
        assertThatThrownBy(() -> QueueEntryId.from("   "))
                .isInstanceOf(CoreException.class)
                .satisfies(error -> assertThat(((CoreException) error).getErrorType()).isEqualTo(ErrorType.INVALID_REQUEST));
    }
}
