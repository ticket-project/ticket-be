package com.ticket.core.domain.showwaitlist.query;

import com.ticket.core.domain.member.query.MemberFinder;
import com.ticket.core.support.cursor.CursorSlice;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetMyShowWaitlistUseCase {

    private static final int MAX_SIZE = 100;

    private final MemberFinder memberFinder;
    private final ShowWaitlistQueryRepository showWaitlistQueryRepository;

    public record Input(Long memberId, String cursor, int size) {
    }

    public record ShowWaitlistSummary(
            Long showId,
            String title,
            String image,
            LocalDate startDate,
            LocalDate endDate,
            LocalDateTime saleStartDate,
            String venue,
            LocalDateTime waitlistedAt
    ) {
    }

    public record Output(Slice<ShowWaitlistSummary> shows, String nextCursor) {
    }

    public Output execute(final Input input) {
        validateInput(input);
        memberFinder.findActiveMemberById(input.memberId());
        final Long cursorWaitlistId = parseCursor(input.cursor());
        final CursorSlice<ShowWaitlistSummary> result =
                showWaitlistQueryRepository.findMyWaitlistedShows(input.memberId(), cursorWaitlistId, input.size());
        return new Output(result.slice(), result.nextCursor());
    }

    private Long parseCursor(final String cursor) {
        if (!StringUtils.hasText(cursor)) {
            return null;
        }
        try {
            return Long.parseLong(cursor);
        } catch (final NumberFormatException exception) {
            throw new CoreException(ErrorType.INVALID_REQUEST, "cursor 형식이 올바르지 않습니다.");
        }
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
