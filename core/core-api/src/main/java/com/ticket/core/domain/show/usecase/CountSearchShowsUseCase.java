package com.ticket.core.domain.show.usecase;

import com.ticket.core.api.controller.request.ShowSearchRequest;
import com.ticket.core.api.controller.response.ShowSearchCountResponse;
import com.ticket.core.domain.show.ShowListQueryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 공연 검색 결과 개수 조회 UseCase
 * - 필터 선택 시 실제 데이터 없이 개수만 반환
 */
@Service
@Transactional(readOnly = true)
public class CountSearchShowsUseCase {
    private final ShowListQueryRepository showListQueryRepository;

    public CountSearchShowsUseCase(final ShowListQueryRepository showListQueryRepository) {
        this.showListQueryRepository = showListQueryRepository;
    }

    public record Input(ShowSearchRequest request) {
    }

    public record Output(ShowSearchCountResponse response) {
    }

    public Output execute(final Input input) {
        long count = showListQueryRepository.countSearchShows(input.request);
        return new Output(new ShowSearchCountResponse(count));
    }
}
