package com.ticket.core.domain.showlike.usecase;

import com.ticket.core.api.controller.response.ShowLikeSummaryResponse;
import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.MemberRepository;
import com.ticket.core.domain.showlike.ShowLikeQueryRepository;
import com.ticket.core.enums.EntityStatus;
import com.ticket.core.support.cursor.CursorSlice;
import com.ticket.core.support.exception.CoreException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetMyShowLikesUseCaseTest {

    @InjectMocks
    private GetMyShowLikesUseCase getMyShowLikesUseCase;

    @Mock
    private ShowLikeQueryRepository showLikeQueryRepository;

    @Mock
    private MemberRepository memberRepository;

    @Test
    void invalid_cursor_throws_exception() {
        mockActiveMember();

        assertThatThrownBy(() -> getMyShowLikesUseCase.execute(
                new GetMyShowLikesUseCase.Input(1L, "invalid-cursor", 20)
        )).isInstanceOf(CoreException.class);
    }

    @Test
    void invalid_size_throws_exception() {
        assertThatThrownBy(() -> getMyShowLikesUseCase.execute(
                new GetMyShowLikesUseCase.Input(1L, null, 0)
        )).isInstanceOf(CoreException.class);
    }

    @Test
    void returns_slice_and_next_cursor_on_success() {
        mockActiveMember();
        final ShowLikeSummaryResponse response = new ShowLikeSummaryResponse(
                10L,
                "test show",
                "https://example.com/test.jpg",
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 2),
                "test venue",
                LocalDateTime.of(2026, 2, 17, 10, 0)
        );
        final Slice<ShowLikeSummaryResponse> slice =
                new SliceImpl<>(List.of(response), PageRequest.of(0, 20), true);
        when(showLikeQueryRepository.findMyLikedShows(1L, 123L, 20))
                .thenReturn(new CursorSlice<>(slice, "122"));

        final GetMyShowLikesUseCase.Output output = getMyShowLikesUseCase.execute(
                new GetMyShowLikesUseCase.Input(1L, "123", 20)
        );

        assertThat(output.shows().getContent()).hasSize(1);
        assertThat(output.shows().getContent().getFirst().title()).isEqualTo("test show");
        assertThat(output.nextCursor()).isEqualTo("122");
    }

    private void mockActiveMember() {
        when(memberRepository.findByIdAndStatus(1L, EntityStatus.ACTIVE))
                .thenReturn(Optional.of(mock(Member.class)));
    }
}
