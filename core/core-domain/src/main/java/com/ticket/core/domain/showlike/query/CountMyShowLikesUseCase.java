package com.ticket.core.domain.showlike.query;

import com.ticket.core.domain.member.query.MemberFinder;
import com.ticket.core.domain.showlike.repository.ShowLikeRepository;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CountMyShowLikesUseCase {

    private final ShowLikeRepository showLikeRepository;
    private final MemberFinder memberFinder;

    public record Input(Long memberId) {
    }

    public record Output(long count) {
    }

    public Output execute(final Input input) {
        validateInput(input);
        memberFinder.findActiveMemberById(input.memberId());

        final long count = showLikeRepository.countByMember_Id(input.memberId());
        return new Output(count);
    }

    private void validateInput(final Input input) {
        if (input == null || input.memberId() == null) {
            throw new CoreException(ErrorType.INVALID_REQUEST, "memberId는 필수입니다.");
        }
    }
}
