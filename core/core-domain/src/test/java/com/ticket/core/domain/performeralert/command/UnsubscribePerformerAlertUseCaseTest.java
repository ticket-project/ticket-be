package com.ticket.core.domain.performeralert.command;

import com.ticket.core.domain.member.model.Member;
import com.ticket.core.domain.member.query.MemberFinder;
import com.ticket.core.domain.performeralert.model.PerformerAlert;
import com.ticket.core.domain.performeralert.repository.PerformerAlertRepository;
import com.ticket.core.domain.show.performer.PerformerFinder;
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
class UnsubscribePerformerAlertUseCaseTest {

    @Mock
    private PerformerAlertRepository performerAlertRepository;
    @Mock
    private MemberFinder memberFinder;
    @Mock
    private PerformerFinder performerFinder;
    @InjectMocks
    private UnsubscribePerformerAlertUseCase useCase;

    @Test
    void 구독_중인_공연자_구독을_취소한다() {
        //given
        when(memberFinder.findActiveMemberById(1L)).thenReturn(mock(Member.class));
        final PerformerAlert performerAlert = mock(PerformerAlert.class);
        when(performerAlertRepository.findByMember_IdAndPerformer_Id(1L, 2L)).thenReturn(Optional.of(performerAlert));

        //when
        UnsubscribePerformerAlertUseCase.Output output = useCase.execute(new UnsubscribePerformerAlertUseCase.Input(1L, 2L));

        //then
        assertThat(output.subscribed()).isFalse();
        assertThat(output.performerId()).isEqualTo(2L);
        verify(performerAlertRepository).delete(performerAlert);
    }

    @Test
    void 구독하지_않은_공연자를_취소해도_멱등하게_성공한다() {
        //given
        when(memberFinder.findActiveMemberById(1L)).thenReturn(mock(Member.class));
        when(performerAlertRepository.findByMember_IdAndPerformer_Id(1L, 2L)).thenReturn(Optional.empty());

        //when
        UnsubscribePerformerAlertUseCase.Output output = useCase.execute(new UnsubscribePerformerAlertUseCase.Input(1L, 2L));

        //then
        assertThat(output.subscribed()).isFalse();
        verify(performerAlertRepository, never()).delete(any());
    }

    @ParameterizedTest
    @MethodSource("invalidInputs")
    void memberId_또는_performerId가_없으면_예외를_던진다(final UnsubscribePerformerAlertUseCase.Input input) {
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
                Arguments.of(new UnsubscribePerformerAlertUseCase.Input(null, 2L)),
                Arguments.of(new UnsubscribePerformerAlertUseCase.Input(1L, null))
        );
    }
}
