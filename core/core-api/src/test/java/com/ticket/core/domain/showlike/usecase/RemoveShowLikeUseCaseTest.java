package com.ticket.core.domain.showlike.usecase;

import com.ticket.core.domain.member.MemberFinder;
import com.ticket.core.domain.show.query.ShowFinder;
import com.ticket.core.domain.showlike.ShowLike;
import com.ticket.core.domain.showlike.ShowLikeRepository;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RemoveShowLikeUseCaseTest {

    @Mock
    private ShowLikeRepository showLikeRepository;
    @Mock
    private MemberFinder memberFinder;
    @Mock
    private ShowFinder showFinder;
    @InjectMocks
    private RemoveShowLikeUseCase useCase;

    @Test
    void 찜이_존재하면_삭제후_false를_반환한다() {
        ShowLike showLike = mock(ShowLike.class);
        when(showLikeRepository.findByMember_IdAndShow_Id(1L, 2L)).thenReturn(Optional.of(showLike));
        when(showLikeRepository.countByShow_Id(2L)).thenReturn(4L);

        RemoveShowLikeUseCase.Output output = useCase.execute(new RemoveShowLikeUseCase.Input(1L, 2L));

        assertThat(output.response().liked()).isFalse();
        assertThat(output.response().likeCount()).isEqualTo(4L);
        verify(showLikeRepository).delete(showLike);
    }

    @Test
    void 입력값이_없으면_예외를_던진다() {
        assertThatThrownBy(() -> useCase.execute(new RemoveShowLikeUseCase.Input(1L, null)))
                .isInstanceOf(CoreException.class)
                .satisfies(exception -> assertThat(((CoreException) exception).getErrorType()).isEqualTo(ErrorType.INVALID_REQUEST));
    }
}
