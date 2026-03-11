package com.ticket.core.domain.showlike.usecase;

import com.ticket.core.api.controller.response.ShowLikeStatusResponse;
import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.MemberFinder;
import com.ticket.core.domain.show.query.ShowFinder;
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
    private final MemberFinder memberFinder;
    private final ShowFinder showFinder;

    public record Input(Long memberId, Long showId) {
    }

    public record Output(ShowLikeStatusResponse response) {
    }

    public Output execute(final Input input) {
        validateInput(input);
        final Member member = memberFinder.findActiveMemberById(input.memberId());
        showFinder.validateShowExists(input.showId());

        final boolean liked = showLikeRepository.existsByMember_IdAndShow_Id(member.getId(), input.showId());
        final long likeCount = showLikeRepository.countByShow_Id(input.showId());
        return new Output(new ShowLikeStatusResponse(input.showId(), liked, likeCount));
    }

    private void validateInput(final Input input) {
        if (input == null || input.memberId() == null || input.showId() == null) {
            throw new CoreException(ErrorType.INVALID_REQUEST, "memberId와 showId는 필수입니다.");
        }
    }
}
