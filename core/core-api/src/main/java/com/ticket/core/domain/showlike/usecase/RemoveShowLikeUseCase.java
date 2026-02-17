package com.ticket.core.domain.showlike.usecase;

import com.ticket.core.api.controller.response.ShowLikeStatusResponse;
import com.ticket.core.domain.show.ShowJpaRepository;
import com.ticket.core.domain.showlike.ShowLikeRepository;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class RemoveShowLikeUseCase {

    private final ShowLikeRepository showLikeRepository;
    private final ShowJpaRepository showJpaRepository;

    public record Input(Long memberId, Long showId) {
    }

    public record Output(ShowLikeStatusResponse response) {
    }

    public Output execute(final Input input) {
        validateInput(input);
        validateShowExists(input.showId());

        showLikeRepository.findByMember_IdAndShow_Id(input.memberId(), input.showId())
                .ifPresent(showLikeRepository::delete);

        return new Output(new ShowLikeStatusResponse(input.showId(), false));
    }

    private void validateInput(final Input input) {
        if (input == null || input.memberId() == null || input.showId() == null) {
            throw new CoreException(ErrorType.INVALID_REQUEST, "memberId와 showId는 필수입니다.");
        }
    }

    private void validateShowExists(final Long showId) {
        if (!showJpaRepository.existsById(showId)) {
            throw new CoreException(ErrorType.NOT_FOUND_DATA, "공연을 찾을 수 없습니다. id=" + showId);
        }
    }
}
