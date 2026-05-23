package com.ticket.core.api.controller;

import com.ticket.core.config.RequireQueueAdmission;
import com.ticket.core.domain.performance.query.GetPerformanceScheduleListUseCase;
import com.ticket.core.domain.performance.query.GetPerformanceSummaryUseCase;
import com.ticket.core.domain.performanceseat.query.GetSeatAvailabilityUseCase;
import com.ticket.core.domain.performanceseat.query.GetSeatStatusUseCase;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
class PerformanceControllerContractTest {

    @Test
    void 좌석_상태_조회_API는_대기열_입장을_요구하지_않는다() throws Exception {
        final Method method = PerformanceController.class.getDeclaredMethod("getSeatStatus", Long.class);

        assertThat(method.isAnnotationPresent(RequireQueueAdmission.class)).isFalse();
    }

    @Test
    void 좌석_잔여수_조회_API는_대기열_입장을_요구하지_않는다() throws Exception {
        final Method method = PerformanceController.class.getDeclaredMethod("getSeatAvailability", Long.class);

        assertThat(method.isAnnotationPresent(RequireQueueAdmission.class)).isFalse();
    }

    @Test
    void 공연_요약_조회_API는_대기열_입장을_요구하지_않는다() throws Exception {
        final Method method = PerformanceController.class.getDeclaredMethod("getPerformanceSummary", Long.class);

        assertThat(method.isAnnotationPresent(RequireQueueAdmission.class)).isFalse();
    }

    @Test
    void 컨트롤러는_기존_의존성으로_생성된다() {
        final PerformanceController controller = new PerformanceController(
                Mockito.mock(GetSeatAvailabilityUseCase.class),
                Mockito.mock(GetSeatStatusUseCase.class),
                Mockito.mock(GetPerformanceSummaryUseCase.class),
                Mockito.mock(GetPerformanceScheduleListUseCase.class)
        );

        assertThat(controller).isNotNull();
    }
}
