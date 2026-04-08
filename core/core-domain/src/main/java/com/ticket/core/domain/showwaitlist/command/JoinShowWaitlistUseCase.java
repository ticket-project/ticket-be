package com.ticket.core.domain.showwaitlist.command;

import com.ticket.core.domain.member.model.Member;
import com.ticket.core.domain.member.query.MemberFinder;
import com.ticket.core.domain.show.model.Show;
import com.ticket.core.domain.show.query.ShowFinder;
import com.ticket.core.domain.showwaitlist.model.ShowWaitlist;
import com.ticket.core.domain.showwaitlist.repository.ShowWaitlistRepository;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class JoinShowWaitlistUseCase {

    private final ShowWaitlistRepository showWaitlistRepository;
    private final MemberFinder memberFinder;
    private final ShowFinder showFinder;

    public record Input(Long memberId, Long showId) {
    }

    public record Output(Long showId, boolean waitlisted, long waitlistCount) {
    }

    public Output execute(final Input input) {
        validateInput(input);

        final Member member = memberFinder.findActiveMemberById(input.memberId());

        if (showWaitlistRepository.existsByMember_IdAndShow_Id(input.memberId(), input.showId())) {
            return new Output(input.showId(), true, countWaitlist(input.showId()));
        }

        final Show show = showFinder.findById(input.showId());

        try {
            showWaitlistRepository.save(new ShowWaitlist(member, show));
        } catch (DataIntegrityViolationException e) {
            throw new CoreException(ErrorType.SHOW_WAITLIST_ALREADY_EXISTS,
                    "이미 대기열에 등록된 공연입니다. memberId=" + input.memberId() + ", showId=" + input.showId());
        }

        return new Output(input.showId(), true, countWaitlist(input.showId()));
    }

    private void validateInput(final Input input) {
        if (input == null || input.memberId() == null || input.showId() == null) {
            throw new CoreException(ErrorType.INVALID_REQUEST, "memberId와 showId는 필수입니다.");
        }
    }

    private long countWaitlist(final Long showId) {
        return showWaitlistRepository.countByShow_Id(showId);
    }
}
