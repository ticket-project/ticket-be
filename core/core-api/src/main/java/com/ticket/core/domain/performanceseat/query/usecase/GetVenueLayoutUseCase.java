package com.ticket.core.domain.performanceseat.query.usecase;

import com.ticket.core.domain.show.Show;
import com.ticket.core.domain.show.query.ShowFinder;
import com.ticket.core.domain.show.venue.Venue;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetVenueLayoutUseCase {

    private final ShowFinder showFinder;

    public record Input(Long showId) {}
    public record Output(@Schema(description = "공연장 이름") String name,
                         @Schema(description = "SVG viewBox 가로") int viewBoxWidth,
                         @Schema(description = "SVG viewBox 세로") int viewBoxHeight,
                         @Schema(description = "좌석 지름") double seatDiameter) {}

    public Output execute(Input input) {
        Show show = showFinder.findById(input.showId());

        Venue venue = show.getVenue();
        if (venue == null) {
            throw new CoreException(ErrorType.NOT_FOUND_DATA,
                    "공연에 연결된 공연장을 찾을 수 없습니다.");
        }

        return new Output(
                venue.getName(),
                venue.getViewBoxWidth(),
                venue.getViewBoxHeight(),
                venue.getSeatDiameter()
        );
    }
}
