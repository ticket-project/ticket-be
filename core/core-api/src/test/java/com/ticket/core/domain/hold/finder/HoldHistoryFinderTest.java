package com.ticket.core.domain.hold.finder;

import com.ticket.core.domain.hold.model.HoldHistory;
import com.ticket.core.domain.hold.repository.HoldHistoryRepository;
import com.ticket.core.enums.HoldState;
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
    void 활성_hold_history만_조회해_반환한다() {
        //given
        HoldHistory first = createHoldHistory(100L);
        HoldHistory second = createHoldHistory(101L);
        when(holdHistoryRepository.findAllByHoldKeyAndStatusOrderByIdAsc("hold-key", HoldState.ACTIVE))
                .thenReturn(List.of(first, second));

        //when
        List<HoldHistory> result = holdHistoryFinder.findActiveByHoldKey("hold-key");

        //then
        assertThat(result).containsExactly(first, second);
        verify(holdHistoryRepository).findAllByHoldKeyAndStatusOrderByIdAsc("hold-key", HoldState.ACTIVE);
    }

    @Test
    void 활성_hold_history가_없으면_빈_목록을_반환한다() {
        //given
        when(holdHistoryRepository.findAllByHoldKeyAndStatusOrderByIdAsc("missing", HoldState.ACTIVE))
                .thenReturn(List.of());

        //when
        List<HoldHistory> result = holdHistoryFinder.findActiveByHoldKey("missing");

        //then
        assertThat(result).isEmpty();
        verify(holdHistoryRepository).findAllByHoldKeyAndStatusOrderByIdAsc("missing", HoldState.ACTIVE);
    }

    private HoldHistory createHoldHistory(final Long seatId) {
        return new HoldHistory(
                "hold-key",
                1L,
                10L,
                seatId + 1000,
                seatId,
                LocalDateTime.of(2026, 3, 15, 12, 30)
        );
    }
}

