package com.ticket.core.domain.performanceseat.usecase;

import com.ticket.core.api.controller.response.ShowSeatResponse;
import com.ticket.core.domain.performanceseat.SeatMapQueryRepository;
import com.ticket.core.domain.show.ShowJpaRepository;
import com.ticket.core.enums.EntityStatus;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetShowSeatsUseCase {

    private final ShowJpaRepository showJpaRepository;
    private final SeatMapQueryRepository seatMapQueryRepository;

    public record Input(Long showId) {}
    public record Output(ShowSeatResponse seatInfo) {}

    public Output execute(Input input) {
        showJpaRepository.findByIdAndStatus(input.showId(), EntityStatus.ACTIVE)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND_DATA, "공연을 찾을 수 없습니다. id=" + input.showId()));

        List<ShowSeatResponse.SeatInfo> seats = seatMapQueryRepository.findShowSeats(input.showId());

        return new Output(new ShowSeatResponse(seats));
    }
}
