package com.ticket.core.domain.show.performer;

import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PerformerFinder {

    private final PerformerJpaRepository performerJpaRepository;

    public Performer findById(final Long performerId) {
        return performerJpaRepository.findById(performerId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA,
                        "공연자를 찾을 수 없습니다. id=" + performerId));
    }

    public void validateExists(final Long performerId) {
        if (!performerJpaRepository.existsById(performerId)) {
            throw new CoreException(ErrorType.NOT_FOUND_DATA,
                    "공연자를 찾을 수 없습니다. id=" + performerId);
        }
    }
}
