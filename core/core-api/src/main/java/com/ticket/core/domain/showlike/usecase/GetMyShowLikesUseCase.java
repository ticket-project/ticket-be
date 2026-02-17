package com.ticket.core.domain.showlike.usecase;

import com.ticket.core.api.controller.response.ShowLikeSummaryResponse;
import com.ticket.core.domain.showlike.ShowLikeQueryRepository;
import com.ticket.core.support.cursor.CursorSlice;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
public class GetMyShowLikesUseCase {

    private static final int MAX_SIZE = 100;

    private final ShowLikeQueryRepository showLikeQueryRepository;

    public GetMyShowLikesUseCase(final ShowLikeQueryRepository showLikeQueryRepository) {
        this.showLikeQueryRepository = showLikeQueryRepository;
    }

    public record Input(Long memberId, String cursor, int size) {
    }

    public record Output(Slice<ShowLikeSummaryResponse> shows, String nextCursor) {
    }

    public Output execute(final Input input) {
        validateInput(input);
        final Long cursorLikeId = parseCursor(input.cursor());
        final CursorSlice<ShowLikeSummaryResponse> result =
                showLikeQueryRepository.findMyLikedShows(input.memberId(), cursorLikeId, input.size());
        return new Output(result.slice(), result.nextCursor());
    }

    private Long parseCursor(final String cursor) {
        if (!StringUtils.hasText(cursor)) {
            return null;
        }

        try {
            return Long.parseLong(cursor);
        } catch (NumberFormatException e) {
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
