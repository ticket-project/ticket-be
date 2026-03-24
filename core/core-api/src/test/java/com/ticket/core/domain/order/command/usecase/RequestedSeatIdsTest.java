package com.ticket.core.domain.order.command.usecase;

import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SuppressWarnings("NonAsciiCharacters")
class RequestedSeatIdsTest {

    @Test
    void 빈_좌석_ID_목록이면_예외를_던진다() {
        assertThatThrownBy(() -> RequestedSeatIds.from(List.of()))
                .isInstanceOf(CoreException.class)
                .satisfies(error -> assertThat(((CoreException) error).getErrorType()).isEqualTo(ErrorType.INVALID_REQUEST));
    }

    @Test
    void 중복된_좌석_ID가_있으면_예외를_던진다() {
        assertThatThrownBy(() -> RequestedSeatIds.from(List.of(3L, 1L, 3L)))
                .isInstanceOf(CoreException.class)
                .satisfies(error -> assertThat(((CoreException) error).getErrorType()).isEqualTo(ErrorType.INVALID_REQUEST));
    }

    @Test
    void 좌석_ID를_오름차순으로_정규화한다() {
        RequestedSeatIds seatIds = RequestedSeatIds.from(List.of(7L, 3L, 5L));

        assertThat(seatIds.values()).containsExactly(3L, 5L, 7L);
    }
}
