package com.ticket.core.domain.show.query.usecase;

import com.ticket.core.domain.show.query.ShowListQueryRepository;
import com.ticket.core.domain.show.query.model.ShowSearchRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 공연 검색 결과 개수 조회 UseCase
 * - 필터 선택 시 실제 데이터 없이 개수만 반환
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CountSearchShowsUseCase {
    private final ShowListQueryRepository showListQueryRepository;

    public record Input(ShowSearchRequest request) {
    }

    public record Output(@Schema(description = "검색 결과 개수", example = "42") long count) {
    }

    public Output execute(final Input input) {
        return new Output(showListQueryRepository.countSearchShows(input.request));
    }
}
