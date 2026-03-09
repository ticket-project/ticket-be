package com.ticket.core.domain.show;

import com.ticket.core.api.controller.response.ShowDetailResponse;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShowFinder {

    private final ShowJpaRepository showJpaRepository;
    private final ShowDetailQueryRepository showDetailQueryRepository;

    public Show findById(final Long showId) {
        return showJpaRepository.findById(showId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA,
                        "공연을 찾을 수 없습니다. id=" + showId));
    }

    public void validateShowExists(final Long showId) {
        if (!showJpaRepository.existsById(showId)) {
            throw new CoreException(ErrorType.NOT_FOUND_DATA,
                    "공연을 찾을 수 없습니다. id=" + showId);
        }
    }

    public ShowDetailResponse findShowDetail(final Long showId) {
        return showDetailQueryRepository.findShowDetail(showId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA,
                        "공연을 찾을 수 없습니다. id=" + showId));
    }
}
