package com.ticket.core.api.controller;

import com.ticket.core.api.controller.request.ShowSearchParam;
import com.ticket.core.api.controller.response.ShowResponse;
import com.ticket.core.domain.show.usecase.SearchShowsUseCase;
import com.ticket.core.support.response.ApiResponse;
import com.ticket.core.support.response.SliceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "공연(Show)", description = "공연 정보 조회 API")
@RestController
@RequestMapping("/api/v1/shows")
public class ShowController {
    private final SearchShowsUseCase searchShowsUseCase;

    public ShowController(final SearchShowsUseCase searchShowsUseCase) {
        this.searchShowsUseCase = searchShowsUseCase;
    }

    @Operation(
            summary = "공연 검색 (무한스크롤)",
            description = """
                    공연 목록을 커서 기반 무한스크롤 방식으로 조회합니다.
                    
                    ## 사용 방법
                    1. **첫 요청**: `cursor` 파라미터 없이 호출
                    2. **다음 페이지**: 응답의 `nextCursor` 값을 `cursor` 파라미터로 전달
                    3. **종료 조건**: `hasNext`가 `false`이면 더 이상 데이터 없음
                    
                    ## 정렬 옵션 (sort 파라미터)
                    - `popular` (기본값) - 인기순 (조회수 높은 순)
                    - `latest` - 최신순 (공연 시작일 최신순)
                    - `endingSoon` - 마감 임박순 (공연 종료일 가까운 순)
                    
                    ## 정렬 예시
                    - `?sort=popular` - 인기순 (기본)
                    - `?sort=latest` - 최신순
                    - `?sort=endingSoon` - 마감 임박순
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "성공 응답 예시",
                                    value = """
                                            {
                                              "status": "SUCCESS",
                                              "data": {
                                                "items": [
                                                  {
                                                    "id": 20,
                                                    "title": "뮤지컬 위키드",
                                                    "subTitle": "10주년 기념 공연",
                                                    "startDate": "2026-03-01",
                                                    "endDate": "2026-05-31",
                                                    "viewCount": 15000,
                                                    "place": "블루스퀘어 신한카드홀"
                                                  },
                                                  {
                                                    "id": 19,
                                                    "title": "콘서트 BTS",
                                                    "subTitle": "월드투어",
                                                    "startDate": "2026-02-28",
                                                    "endDate": "2026-03-02",
                                                    "viewCount": 50000,
                                                    "place": "잠실올림픽주경기장"
                                                  }
                                                ],
                                                "hasNext": true,
                                                "size": 5,
                                                "numberOfElements": 2,
                                                "nextCursor": "eyJpZCI6MTksInN0YXJ0RGF0ZSI6IjIwMjYtMDItMjgifQ=="
                                              },
                                              "message": null
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping
    public ApiResponse<SliceResponse<ShowResponse>> searchShows(
            @ParameterObject
            final ShowSearchParam param,

            @Parameter(description = "한 번에 조회할 개수 (기본값: 5, 최대: 100)", example = "5")
            @RequestParam(defaultValue = "5") final int size,

            @Parameter(description = "정렬 기준 [popular(인기순), latest(최신순), endingSoon(마감임박순)]", example = "popular")
            @RequestParam(defaultValue = "popular") final String sort
    ) {
        final SearchShowsUseCase.Input input = new SearchShowsUseCase.Input(param, size, sort);
        final SearchShowsUseCase.Output output = searchShowsUseCase.execute(input);
        return ApiResponse.success(SliceResponse.from(output.shows(), output.nextCursor()));
    }

}

