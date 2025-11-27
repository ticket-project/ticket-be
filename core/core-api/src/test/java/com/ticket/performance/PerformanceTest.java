package com.ticket.performance;

import com.ticket.core.domain.performance.Performance;
import com.ticket.core.support.exception.CoreException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SuppressWarnings("NonAsciiCharacters")
public class PerformanceTest {
    @Test
    void 공연_시작_시간이_종료_시간보다_빠르면_예외가_발생한다() {
        //given
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = LocalDateTime.now().minusHours(1);
        //when
        assertThatThrownBy(() -> new Performance(startTime, endTime)).isInstanceOf(CoreException.class);
        //then

    }
}
