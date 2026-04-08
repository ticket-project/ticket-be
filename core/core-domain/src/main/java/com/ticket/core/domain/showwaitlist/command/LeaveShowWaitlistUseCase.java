package com.ticket.core.domain.showwaitlist.command;

import com.ticket.core.domain.member.query.MemberFinder;
import com.ticket.core.domain.show.query.ShowFinder;
import com.ticket.core.domain.showwaitlist.repository.ShowWaitlistRepository;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class LeaveShowWaitlistUseCase {

    private final ShowWaitlistRepository showWaitlistRepository;
    private final MemberFinder memberFinder;
    private final ShowFinder showFinder;

    public record Input(Long memberId, Long showId) {
    }

    public record Output(Long showId, boolean waitlisted, long waitlistCount) {
    }

    public Output execute(final Input input) {
        validateInput(input);

        memberFinder.findActiveMemberById(input.memberId());
        showFinder.validateShowExists(input.showId());

        showWaitlistRepository.findByMember_IdAndShow_Id(input.memberId(), input.showId())
                .ifPresent(showWaitlistRepository::delete);

        return new Output(input.showId(), false, showWaitlistRepository.countByShow_Id(input.showId()));
    }

    private void validateInput(final Input input) {
        if (input == null || input.memberId() == null || input.showId() == null) {
            throw new CoreException(ErrorType.INVALID_REQUEST, "memberId와 showId는 필수입니다.");
        }
    }
}
