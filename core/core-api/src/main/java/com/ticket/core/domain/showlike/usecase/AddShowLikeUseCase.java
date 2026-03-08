package com.ticket.core.domain.showlike.usecase;

import com.ticket.core.api.controller.response.ShowLikeStatusResponse;
import com.ticket.core.domain.member.Member;
import com.ticket.core.domain.member.MemberFinder;
import com.ticket.core.domain.show.Show;
import com.ticket.core.domain.show.ShowFinder;
import com.ticket.core.domain.showlike.ShowLike;
import com.ticket.core.domain.showlike.ShowLikeRepository;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AddShowLikeUseCase {

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

        if (showLikeRepository.existsByMember_IdAndShow_Id(input.memberId(), input.showId())) {
            return new Output(new ShowLikeStatusResponse(input.showId(), true, countLikes(input.showId())));
        }

        final Show show = showFinder.findActiveShow(input.showId());

        try {
            showLikeRepository.save(new ShowLike(member, show));
        } catch (DataIntegrityViolationException e) {
            throw new CoreException(ErrorType.SHOW_LIKE_ALREADY_EXISTS,
                    "이미 찜한 공연입니다. memberId=" + input.memberId() + ", showId=" + input.showId());
        }

        return new Output(new ShowLikeStatusResponse(input.showId(), true, countLikes(input.showId())));
    }

    private void validateInput(final Input input) {
        if (input == null || input.memberId() == null || input.showId() == null) {
            throw new CoreException(ErrorType.INVALID_REQUEST, "memberId와 showId는 필수입니다.");
        }
    }

    private long countLikes(final Long showId) {
        return showLikeRepository.countByShow_Id(showId);
    }
}