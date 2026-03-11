package com.ticket.core.domain.hold.finder;

import com.ticket.core.domain.hold.model.HoldHistory;
import com.ticket.core.domain.hold.repository.HoldHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class HoldHistoryFinder {

    private final HoldHistoryRepository holdHistoryRepository;

    public List<HoldHistory> findByHoldToken(final String holdToken) {
        return holdHistoryRepository.findAllByHoldTokenOrderByIdAsc(holdToken);
    }
}
