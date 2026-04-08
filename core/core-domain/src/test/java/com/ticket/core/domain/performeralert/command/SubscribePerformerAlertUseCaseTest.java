package com.ticket.core.domain.performeralert.command;

import com.ticket.core.domain.member.model.Member;
import com.ticket.core.domain.member.query.MemberFinder;
import com.ticket.core.domain.performeralert.repository.PerformerAlertRepository;
import com.ticket.core.domain.show.performer.Performer;
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
import org.springframework.dao.DataIntegrityViolationException;

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
class SubscribePerformerAlertUseCaseTest {

    @Mock
    private PerformerAlertRepository performerAlertRepository;
    @Mock
    private MemberFinder memberFinder;
    @Mock
    private PerformerFinder performerFinder;
    @InjectMocks
    private SubscribePerformerAlertUseCase useCase;

    @Test
    void 이미_구독_중인_공연자면_저장하지_않고_상태만_반환한다() {
        //given
        when(memberFinder.findActiveMemberById(1L)).thenReturn(mock(Member.class));
        when(performerAlertRepository.existsByMember_IdAndPerformer_Id(1L, 2L)).thenReturn(true);

        //when
        SubscribePerformerAlertUseCase.Output output = useCase.execute(new SubscribePerformerAlertUseCase.Input(1L, 2L));

        //then
        assertThat(output.subscribed()).isTrue();
        assertThat(output.performerId()).isEqualTo(2L);
        verify(performerAlertRepository, never()).save(any());
    }

    @Test
    void 새로_구독하면_저장_후_상태를_반환한다() {
        //given
        when(memberFinder.findActiveMemberById(1L)).thenReturn(mock(Member.class));
        when(performerAlertRepository.existsByMember_IdAndPerformer_Id(1L, 2L)).thenReturn(false);
        when(performerFinder.findById(2L)).thenReturn(mock(Performer.class));

        //when
        SubscribePerformerAlertUseCase.Output output = useCase.execute(new SubscribePerformerAlertUseCase.Input(1L, 2L));

        //then
        assertThat(output.subscribed()).isTrue();
        verify(performerAlertRepository).save(any());
    }

    @Test
    void 저장중_중복제약이_발생하면_예외를_던진다() {
        //given
        when(memberFinder.findActiveMemberById(1L)).thenReturn(mock(Member.class));
        when(performerAlertRepository.existsByMember_IdAndPerformer_Id(1L, 2L)).thenReturn(false);
        when(performerFinder.findById(2L)).thenReturn(mock(Performer.class));
        when(performerAlertRepository.save(any())).thenThrow(new DataIntegrityViolationException("duplicate"));

        //when
        //then
        assertThatThrownBy(() -> useCase.execute(new SubscribePerformerAlertUseCase.Input(1L, 2L)))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> assertThat(((CoreException) exception).getErrorType())
                        .isEqualTo(ErrorType.PERFORMER_ALERT_ALREADY_EXISTS));
    }

    @ParameterizedTest
    @MethodSource("invalidInputs")
    void memberId_또는_performerId가_없으면_예외를_던진다(final SubscribePerformerAlertUseCase.Input input) {
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
                Arguments.of(new SubscribePerformerAlertUseCase.Input(null, 2L)),
                Arguments.of(new SubscribePerformerAlertUseCase.Input(1L, null))
        );
    }
}
