package com.ticket.core.domain.showlike.usecase;

import com.ticket.core.api.controller.response.ShowLikeStatusResponse;
import com.ticket.core.domain.member.MemberRepository;
import com.ticket.core.enums.EntityStatus;
import com.ticket.core.domain.show.ShowJpaRepository;
import com.ticket.core.domain.showlike.ShowLikeRepository;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetShowLikeStatusUseCase {

    private final ShowLikeRepository showLikeRepository;
    private final MemberRepository memberRepository;
    private final ShowJpaRepository showJpaRepository;

    public record Input(Long memberId, Long showId) {
    }

    public record Output(ShowLikeStatusResponse response) {
    }

    public Output execute(final Input input) {
        validateInput(input);
        validateMemberExists(input.memberId());
        validateShowExists(input.showId());

        final boolean liked = showLikeRepository.existsByMember_IdAndShow_Id(input.memberId(), input.showId());
        return new Output(new ShowLikeStatusResponse(input.showId(), liked));
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

    private void validateMemberExists(final Long memberId) {
        if (memberRepository.findByIdAndStatus(memberId, EntityStatus.ACTIVE).isEmpty()) {
            throw new CoreException(ErrorType.NOT_FOUND_DATA, "member not found. id=" + memberId);
        }
    }
}
