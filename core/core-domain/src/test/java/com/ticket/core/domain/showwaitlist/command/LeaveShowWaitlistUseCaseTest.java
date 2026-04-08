package com.ticket.core.domain.showwaitlist.command;

import com.ticket.core.domain.member.model.Member;
import com.ticket.core.domain.member.query.MemberFinder;
import com.ticket.core.domain.show.query.ShowFinder;
import com.ticket.core.domain.showwaitlist.model.ShowWaitlist;
import com.ticket.core.domain.showwaitlist.repository.ShowWaitlistRepository;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
class LeaveShowWaitlistUseCaseTest {

    @Mock
    private ShowWaitlistRepository showWaitlistRepository;
    @Mock
    private MemberFinder memberFinder;
    @Mock
    private ShowFinder showFinder;
    @InjectMocks
    private LeaveShowWaitlistUseCase useCase;

    @Test
    void 대기열에_등록된_공연을_탈퇴한다() {
        //given
        when(memberFinder.findActiveMemberById(1L)).thenReturn(mock(Member.class));
        final ShowWaitlist showWaitlist = mock(ShowWaitlist.class);
        when(showWaitlistRepository.findByMember_IdAndShow_Id(1L, 2L)).thenReturn(Optional.of(showWaitlist));
        when(showWaitlistRepository.countByShow_Id(2L)).thenReturn(3L);

        //when
        LeaveShowWaitlistUseCase.Output output = useCase.execute(new LeaveShowWaitlistUseCase.Input(1L, 2L));

        //then
        assertThat(output.waitlisted()).isFalse();
        assertThat(output.showId()).isEqualTo(2L);
        verify(showWaitlistRepository).delete(showWaitlist);
    }

    @Test
    void 대기열에_등록되지_않은_공연을_탈퇴해도_멱등하게_성공한다() {
        //given
        when(memberFinder.findActiveMemberById(1L)).thenReturn(mock(Member.class));
        when(showWaitlistRepository.findByMember_IdAndShow_Id(1L, 2L)).thenReturn(Optional.empty());
        when(showWaitlistRepository.countByShow_Id(2L)).thenReturn(0L);

        //when
        LeaveShowWaitlistUseCase.Output output = useCase.execute(new LeaveShowWaitlistUseCase.Input(1L, 2L));

        //then
        assertThat(output.waitlisted()).isFalse();
        verify(showWaitlistRepository, never()).delete(any());
    }

    @ParameterizedTest
    @MethodSource("invalidInputs")
    void memberId_또는_showId가_없으면_예외를_던진다(final LeaveShowWaitlistUseCase.Input input) {
        //given
        //when
        //then
        assertThatThrownBy(() -> useCase.execute(input))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> assertThat(((CoreException) exception).getErrorType())
                        .isEqualTo(ErrorType.INVALID_REQUEST));
    }

    private static Stream<Arguments> invalidInputs() {
        return Stream.of(
                Arguments.of((Object) null),
                Arguments.of(new LeaveShowWaitlistUseCase.Input(null, 2L)),
                Arguments.of(new LeaveShowWaitlistUseCase.Input(1L, null))
        );
    }
}
