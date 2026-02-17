package com.ticket.core.domain.showlike.usecase;

import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.MemberRepository;
import com.ticket.core.domain.show.Show;
import com.ticket.core.domain.show.ShowJpaRepository;
import com.ticket.core.domain.showlike.ShowLike;
import com.ticket.core.domain.showlike.ShowLikeRepository;
import com.ticket.core.support.exception.CoreException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddShowLikeUseCaseTest {

    @InjectMocks
    private AddShowLikeUseCase addShowLikeUseCase;

    @Mock
    private ShowLikeRepository showLikeRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private ShowJpaRepository showJpaRepository;

    @Test
    void 이미_찜한_공연이면_저장하지_않고_성공을_반환한다() {
        // given
        final Long memberId = 1L;
        final Long showId = 10L;
        when(showLikeRepository.existsByMember_IdAndShow_Id(memberId, showId)).thenReturn(true);

        // when
        final AddShowLikeUseCase.Output output = addShowLikeUseCase.execute(
                new AddShowLikeUseCase.Input(memberId, showId)
        );

        // then
        assertThat(output.response().showId()).isEqualTo(showId);
        assertThat(output.response().liked()).isTrue();
        verify(showLikeRepository, never()).save(any(ShowLike.class));
        verifyNoInteractions(memberRepository, showJpaRepository);
    }

    @Test
    void 찜이_없는_경우_저장하고_성공을_반환한다() {
        // given
        final Long memberId = 1L;
        final Long showId = 20L;
        final Member member = mock(Member.class);
        final Show show = mock(Show.class);

        when(showLikeRepository.existsByMember_IdAndShow_Id(memberId, showId)).thenReturn(false);
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(showJpaRepository.findById(showId)).thenReturn(Optional.of(show));

        // when
        final AddShowLikeUseCase.Output output = addShowLikeUseCase.execute(
                new AddShowLikeUseCase.Input(memberId, showId)
        );

        // then
        assertThat(output.response().showId()).isEqualTo(showId);
        assertThat(output.response().liked()).isTrue();
        verify(showLikeRepository, times(1)).save(any(ShowLike.class));
    }

    @Test
    void 공연이_없으면_예외가_발생한다() {
        // given
        final Long memberId = 1L;
        final Long showId = 404L;
        final Member member = mock(Member.class);

        when(showLikeRepository.existsByMember_IdAndShow_Id(memberId, showId)).thenReturn(false);
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(showJpaRepository.findById(showId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> addShowLikeUseCase.execute(new AddShowLikeUseCase.Input(memberId, showId)))
                .isInstanceOf(CoreException.class);
        verify(showLikeRepository, never()).save(any(ShowLike.class));
    }
}
