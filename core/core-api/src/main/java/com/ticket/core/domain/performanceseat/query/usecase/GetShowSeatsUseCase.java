package com.ticket.core.domain.performanceseat.query.usecase;

import com.ticket.core.api.controller.response.ShowSeatResponse;
import com.ticket.core.domain.performanceseat.query.SeatMapQueryRepository;
import com.ticket.core.domain.show.Show;
import com.ticket.core.domain.show.query.ShowFinder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetShowSeatsUseCase {

    private final ShowFinder showFinder;
    private final SeatMapQueryRepository seatMapQueryRepository;

    public record Input(Long showId) {}
    public record Output(ShowSeatResponse seatInfo) {}

    public Output execute(Input input) {
        final Show show = showFinder.findById(input.showId());

        List<ShowSeatResponse.SeatInfo> seats = seatMapQueryRepository.findShowSeats(show.getId());

        return new Output(new ShowSeatResponse(seats));
    }
}
