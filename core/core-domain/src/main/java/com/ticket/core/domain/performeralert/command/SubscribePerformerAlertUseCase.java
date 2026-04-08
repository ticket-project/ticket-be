package com.ticket.core.domain.performeralert.command;

import com.ticket.core.domain.member.model.Member;
import com.ticket.core.domain.member.query.MemberFinder;
import com.ticket.core.domain.performeralert.model.PerformerAlert;
import com.ticket.core.domain.performeralert.repository.PerformerAlertRepository;
import com.ticket.core.domain.show.performer.Performer;
import com.ticket.core.domain.show.performer.PerformerFinder;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class SubscribePerformerAlertUseCase {

    private final PerformerAlertRepository performerAlertRepository;
    private final MemberFinder memberFinder;
    private final PerformerFinder performerFinder;

    public record Input(Long memberId, Long performerId) {
    }

    public record Output(Long performerId, boolean subscribed) {
    }

    public Output execute(final Input input) {
        validateInput(input);

        final Member member = memberFinder.findActiveMemberById(input.memberId());

        if (performerAlertRepository.existsByMember_IdAndPerformer_Id(input.memberId(), input.performerId())) {
            return new Output(input.performerId(), true);
        }

        final Performer performer = performerFinder.findById(input.performerId());

        try {
            performerAlertRepository.save(new PerformerAlert(member, performer));
        } catch (DataIntegrityViolationException e) {
            throw new CoreException(ErrorType.PERFORMER_ALERT_ALREADY_EXISTS,
                    "이미 구독 중인 공연자입니다. memberId=" + input.memberId() + ", performerId=" + input.performerId());
        }

        return new Output(input.performerId(), true);
    }

    private void validateInput(final Input input) {
        if (input == null || input.memberId() == null || input.performerId() == null) {
            throw new CoreException(ErrorType.INVALID_REQUEST, "memberId와 performerId는 필수입니다.");
        }
    }
}
