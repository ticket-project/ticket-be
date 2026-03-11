package com.ticket.core.domain.hold;

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
