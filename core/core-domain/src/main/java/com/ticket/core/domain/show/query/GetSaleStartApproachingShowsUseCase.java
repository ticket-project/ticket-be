package com.ticket.core.domain.show.query;

import com.ticket.core.domain.show.query.ShowListQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetSaleStartApproachingShowsUseCase {
    private final ShowListQueryRepository showListQueryRepository;

    public record Input(String category, int size) {
    }

    public record Output(List<ShowOpeningSoonSummary> shows) {
    }

    public record ShowOpeningSoonSummary(
            Long id,
            String title,
            String image,
            String venue,
            LocalDateTime saleStartDate
    ) {
    }

    public Output execute(final Input input) {
        return new Output(showListQueryRepository.findShowsSaleOpeningSoon(input.category, input.size));
    }
}
