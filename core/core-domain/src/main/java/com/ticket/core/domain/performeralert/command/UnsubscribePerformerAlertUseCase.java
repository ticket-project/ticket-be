package com.ticket.core.domain.performeralert.command;

import com.ticket.core.domain.member.query.MemberFinder;
import com.ticket.core.domain.performeralert.repository.PerformerAlertRepository;
import com.ticket.core.domain.show.performer.PerformerFinder;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UnsubscribePerformerAlertUseCase {

    private final PerformerAlertRepository performerAlertRepository;
    private final MemberFinder memberFinder;
    private final PerformerFinder performerFinder;

    public record Input(Long memberId, Long performerId) {
    }

    public record Output(Long performerId, boolean subscribed) {
    }

    public Output execute(final Input input) {
        validateInput(input);

        memberFinder.findActiveMemberById(input.memberId());
        performerFinder.validateExists(input.performerId());

        performerAlertRepository.findByMember_IdAndPerformer_Id(input.memberId(), input.performerId())
                .ifPresent(performerAlertRepository::delete);

        return new Output(input.performerId(), false);
    }

    private void validateInput(final Input input) {
        if (input == null || input.memberId() == null || input.performerId() == null) {
            throw new CoreException(ErrorType.INVALID_REQUEST, "memberId와 performerId는 필수입니다.");
        }
    }
}
