package com.ticket.core.domain.hold.query;

import com.ticket.core.domain.hold.model.HoldHistory;
import com.ticket.core.domain.hold.repository.HoldHistoryRepository;
import com.ticket.core.domain.hold.model.HoldReleaseReason;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
class HoldHistoryFinderTest {

    @Mock
    private HoldHistoryRepository holdHistoryRepository;

    @InjectMocks
    private HoldHistoryFinder holdHistoryFinder;

    @Test
    void hold_key로_이력을_순서대로_조회한다() {
        //given
        HoldHistory first = HoldHistory.created(
                "hold-key",
                1L,
                10L,
                1100L,
                100L,
                LocalDateTime.of(2026, 3, 15, 12, 0),
                LocalDateTime.of(2026, 3, 15, 12, 30)
        );
        HoldHistory second = HoldHistory.canceled(
                "hold-key",
                1L,
                10L,
                1101L,
                101L,
                LocalDateTime.of(2026, 3, 15, 12, 10),
                HoldReleaseReason.USER_CANCELED
        );
        when(holdHistoryRepository.findAllByHoldKeyOrderByIdAsc("hold-key"))
                .thenReturn(List.of(first, second));

        //when
        List<HoldHistory> result = holdHistoryFinder.findByHoldKey("hold-key");

        //then
        assertThat(result).containsExactly(first, second);
        verify(holdHistoryRepository).findAllByHoldKeyOrderByIdAsc("hold-key");
    }

    @Test
    void 이력이_없으면_빈_목록을_반환한다() {
        //given
        when(holdHistoryRepository.findAllByHoldKeyOrderByIdAsc("missing"))
                .thenReturn(List.of());

        //when
        List<HoldHistory> result = holdHistoryFinder.findByHoldKey("missing");

        //then
        assertThat(result).isEmpty();
        verify(holdHistoryRepository).findAllByHoldKeyOrderByIdAsc("missing");
    }
}
