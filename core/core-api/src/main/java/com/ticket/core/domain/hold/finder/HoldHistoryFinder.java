package com.ticket.core.domain.hold.finder;

import com.ticket.core.domain.hold.model.HoldHistory;
import com.ticket.core.domain.hold.repository.HoldHistoryRepository;
import com.ticket.core.enums.HoldState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class HoldHistoryFinder {

    private final HoldHistoryRepository holdHistoryRepository;

    public List<HoldHistory> findActiveByHoldKey(final String holdKey) {
        return holdHistoryRepository.findAllByHoldKeyAndStatusOrderByIdAsc(holdKey, HoldState.ACTIVE);
    }
}
