package com.ticket.core.domain.showlike.usecase;

import com.ticket.core.api.controller.response.ShowLikeSummaryResponse;
import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.MemberFinder;
import com.ticket.core.domain.showlike.ShowLikeQueryRepository;
import com.ticket.core.support.cursor.CursorSlice;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.SliceImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetMyShowLikesUseCaseTest {

    @Mock
    private MemberFinder memberFinder;
    @Mock
    private ShowLikeQueryRepository showLikeQueryRepository;
    @InjectMocks
    private GetMyShowLikesUseCase useCase;

    @Test
    void 찜한_공연_목록을_커서와_함께_조회한다() {
        Member member = mock(Member.class);
        when(member.getId()).thenReturn(1L);
        when(memberFinder.findActiveMemberById(1L)).thenReturn(member);
        ShowLikeSummaryResponse summary = new ShowLikeSummaryResponse(2L, "공연", "image", LocalDate.now(), LocalDate.now().plusDays(1), "장소", LocalDateTime.now());
        when(showLikeQueryRepository.findMyLikedShows(1L, 10L, 20))
                .thenReturn(new CursorSlice<>(new SliceImpl<>(List.of(summary)), "9"));

        GetMyShowLikesUseCase.Output output = useCase.execute(new GetMyShowLikesUseCase.Input(1L, "10", 20));

        assertThat(output.shows().getContent()).containsExactly(summary);
        assertThat(output.nextCursor()).isEqualTo("9");
        verify(showLikeQueryRepository).findMyLikedShows(1L, 10L, 20);
    }

    @Test
    void cursor가_숫자가_아니면_예외를_던진다() {
        assertThatThrownBy(() -> useCase.execute(new GetMyShowLikesUseCase.Input(1L, "abc", 20)))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> assertThat(((CoreException) exception).getErrorType()).isEqualTo(ErrorType.INVALID_REQUEST));
    }
}
