package com.ticket.core.domain.show.usecase;

import com.ticket.core.api.controller.response.ShowDetailResponse;
import com.ticket.core.domain.show.ShowDetailQueryRepository;
import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetShowDetailUseCase {

    private final ShowDetailQueryRepository showDetailQueryRepository;

    public record Input(Long showId) {}

    public record Output(ShowDetailResponse show) {}

    public Output execute(Input input) {
        ShowDetailResponse detail = showDetailQueryRepository.findShowDetail(input.showId())
                .orElseThrow(() -> new CoreException(
                        ErrorType.NOT_FOUND_DATA,
                        "공연을 찾을 수 없습니다. id=" + input.showId()
                ));
        return new Output(detail);
    }
}
