package com.ticket.core.domain.performanceseat.usecase;

import com.ticket.core.api.controller.response.VenueLayoutResponse;
import com.ticket.core.domain.show.Show;
import com.ticket.core.domain.show.ShowJpaRepository;
import com.ticket.core.domain.show.Venue;
import com.ticket.core.enums.EntityStatus;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetVenueLayoutUseCase {

    private final ShowJpaRepository showJpaRepository;

    public record Input(Long showId) {}
    public record Output(VenueLayoutResponse layout) {}

    public Output execute(Input input) {
        Show show = showJpaRepository.findByIdAndStatus(input.showId(), EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(
                        ErrorType.NOT_FOUND_DATA,
                        "공연을 찾을 수 없습니다. id=" + input.showId()
                ));

        Venue venue = show.getVenue();
        if (venue == null) {
            throw new CoreException(ErrorType.NOT_FOUND_DATA,
                    "공연에 연결된 공연장을 찾을 수 없습니다.");
        }

        VenueLayoutResponse layout = new VenueLayoutResponse(
                venue.getName(),
                venue.getViewBoxWidth(),
                venue.getViewBoxHeight(),
                venue.getSeatDiameter()
        );

        return new Output(layout);
    }
}
