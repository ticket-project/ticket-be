package com.ticket.performance;

import com.ticket.core.domain.performance.Performance;
import com.ticket.core.domain.performanceseat.PerformanceSeat;
import com.ticket.core.support.exception.CoreException;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;

import static com.ticket.util.TestCommonUtils.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SuppressWarnings("NonAsciiCharacters")
public class PerformanceTest {
    @Test
    void 회차_시작_시간이_종료_시간보다_느리면_예외가_발생한다() {
        //then
        assertThatThrownBy(() -> new Performance(currentTime(), currentTime().minusHours(1), reserveOpenTimeFuture(), reserveCloseTimeFuture())).isInstanceOf(CoreException.class);
    }

    @Test
        //todo 이렇게 시간 설정을 하게 되면 나중에 reserveOpenTime이 지나고 테스트를 돌리면 이 결과는 바뀐다. -> 수정 필요.
    void 예매_오픈_시간_전에는_예매가_불가능하다() {
        //given
        int seatCount = 4;
        Long userId = 1L;
        final Performance performance = new Performance(performanceStartTime(), performanceEndTime(), reserveOpenTimeFuture(), reserveCloseTimeFuture());
        final PerformanceSeat performanceSeat = new PerformanceSeat(performance, seatCount);
        //then
        AssertionsForClassTypes.assertThatThrownBy(() -> performanceSeat.reserve(userId, currentTime())).isInstanceOf(CoreException.class);
    }

}
