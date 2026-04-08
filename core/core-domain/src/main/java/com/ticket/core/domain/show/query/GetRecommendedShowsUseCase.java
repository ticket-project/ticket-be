package com.ticket.core.domain.show.query;

import com.ticket.core.domain.member.query.MemberFinder;
import com.ticket.core.domain.show.query.model.ShowSummaryView;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetRecommendedShowsUseCase {

    private static final int MAX_SIZE = 50;

    private final MemberFinder memberFinder;
    private final ShowRecommendationQueryRepository showRecommendationQueryRepository;

    public record Input(Long memberId, int size) {
    }

    public record Output(List<ShowSummaryView> shows) {
    }

    public Output execute(final Input input) {
        validateInput(input);
        memberFinder.findActiveMemberById(input.memberId());
        final List<ShowSummaryView> shows =
                showRecommendationQueryRepository.findRecommendedShows(input.memberId(), input.size());
        return new Output(shows);
    }

    private void validateInput(final Input input) {
        if (input == null || input.memberId() == null) {
            throw new CoreException(ErrorType.INVALID_REQUEST, "memberId는 필수입니다.");
        }
        if (input.size() <= 0 || input.size() > MAX_SIZE) {
            throw new CoreException(ErrorType.INVALID_REQUEST, "size는 1 이상 " + MAX_SIZE + " 이하여야 합니다.");
        }
    }
}
