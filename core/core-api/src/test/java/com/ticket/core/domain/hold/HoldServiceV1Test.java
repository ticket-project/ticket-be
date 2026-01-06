package com.ticket.core.domain.hold;

import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.MemberFinder;
import com.ticket.core.domain.performance.Performance;
import com.ticket.core.domain.performance.PerformanceFinder;
import com.ticket.core.domain.performanceseat.PerformanceSeat;
import com.ticket.core.domain.performanceseat.PerformanceSeatFinder;
import com.ticket.core.support.TestDataFactory;
import com.ticket.core.support.exception.CoreException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
class HoldServiceV1Test {

    @InjectMocks
    private HoldServiceV1 holdService;
    @Mock
    private MemberFinder memberFinder;
    @Mock
    private PerformanceFinder performanceFinder;
    @Mock
    private PerformanceSeatFinder performanceSeatFinder;

    @Test
    void 선점_요청한_좌석_중_하나라도_불가능하면_전체가_실패한다() {
        // given
        final Member member = TestDataFactory.createMember();
        final Performance performance = TestDataFactory.createPerformance();
        final List<PerformanceSeat> availableSeats = TestDataFactory.createAvailableSeats(performance.getId(), List.of(1L, 2L));
        final List<Long> seatIds = availableSeats.stream().map(PerformanceSeat::getSeatId).toList();
        when(memberFinder.find(member.getId())).thenReturn(member);
        when(performanceFinder.findOpenPerformance(performance.getId())).thenReturn(performance);
        when(performanceSeatFinder.findAvailablePerformanceSeats(seatIds, performance.getId())).thenReturn(Collections.emptyList());
        NewSeatHold newSeatHold = new NewSeatHold(member.getId(), performance.getId(), seatIds);
        // when & then
        assertThatThrownBy(() -> holdService.hold(newSeatHold))
                .isInstanceOf(CoreException.class);
    }
}
