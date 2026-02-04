package com.ticket.core.api.controller;

import com.ticket.core.api.controller.request.SaleOpeningSoonSearchParam;
import com.ticket.core.api.controller.request.ShowSearchParam;
import com.ticket.core.api.controller.response.ShowOpeningSoonDetailResponse;
import com.ticket.core.api.controller.response.ShowResponse;
import com.ticket.core.domain.show.usecase.GetLatestShowsUseCase;
import com.ticket.core.domain.show.usecase.GetShowsOpeningSoonUseCase;
import com.ticket.core.domain.show.usecase.GetShowsSaleOpeningSoonPageUseCase;
import com.ticket.core.domain.show.usecase.GetShowsUseCase;
import com.ticket.core.support.response.ApiResponse;
import com.ticket.core.support.response.SliceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "공연(Show)", description = "공연 정보 조회 API")
@RestController
@RequestMapping("/api/v1/shows")
@Validated
public class ShowController {
    private final GetShowsUseCase getShowsUseCase;
    private final GetLatestShowsUseCase getLatestShowsUseCase;
    private final GetShowsOpeningSoonUseCase getShowsOpeningSoonUseCase;
    private final GetShowsSaleOpeningSoonPageUseCase getShowsSaleOpeningSoonPageUseCase;

    public ShowController(
            final GetShowsUseCase getShowsUseCase,
            final GetLatestShowsUseCase getLatestShowsUseCase,
            final GetShowsOpeningSoonUseCase getShowsOpeningSoonUseCase,
            final GetShowsSaleOpeningSoonPageUseCase getShowsSaleOpeningSoonPageUseCase
    ) {
        this.getShowsUseCase = getShowsUseCase;
        this.getLatestShowsUseCase = getLatestShowsUseCase;
        this.getShowsOpeningSoonUseCase = getShowsOpeningSoonUseCase;
        this.getShowsSaleOpeningSoonPageUseCase = getShowsSaleOpeningSoonPageUseCase;
    }

    @Operation(
            summary = "공연 조회 (무한스크롤)",
            description = """
                    공연 목록을 커서 기반 무한스크롤 방식으로 조회합니다.
                    
                    ## 사용 방법
                    1. **첫 요청**: `cursor` 파라미터 없이 호출
                    2. **다음 페이지**: 응답의 `nextCursor` 값을 `cursor` 파라미터로 전달
                    3. **종료 조건**: `hasNext`가 `false`이면 더 이상 데이터 없음
                    
                    ## 정렬 옵션 (sort 파라미터)
                    - `popular` (기본값) - 인기순 (조회수 높은 순)
                    - `latest` - 최신순 (생성일 최신순)
                    - `approaching` - 공연 임박순 (공연 시작일 가까운 순)
                    
                    ## 정렬 예시
                    - `?sort=popular` - 인기순 (기본)
                    - `?sort=latest` - 최신순
                    - `?sort=approaching` - 공연 임박순
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
                                              "result": "SUCCESS",
                                              "data": {
                                                "items": [
                                                  {
                                                    "id": 20,
                                                    "title": "뮤지컬 위키드",
                                                    "subTitle": "10주년 기념 공연",
                                                    "categoryName": "뮤지컬",
                                                    "startDate": "2026-03-01",
                                                    "endDate": "2026-05-31",
                                                    "viewCount": 15000,
                                                    "saleType": "GENERAL",
                                                    "saleStartDate": "2026-01-01",
                                                    "saleEndDate": "2026-02-28",
                                                    "createdAt": "2026-01-01T10:00:00",
                                                    "region": "SEOUL",
                                                    "venue": "블루스퀘어 신한카드홀"
                                                  },
                                                  {
                                                    "id": 19,
                                                    "title": "콘서트 BTS",
                                                    "subTitle": "월드투어",
                                                    "categoryName": "콘서트",
                                                    "startDate": "2026-02-28",
                                                    "endDate": "2026-03-02",
                                                    "viewCount": 50000,
                                                    "saleType": "EXCLUSIVE",
                                                    "saleStartDate": "2026-01-15",
                                                    "saleEndDate": "2026-02-15",
                                                    "createdAt": "2026-01-01T12:00:00",
                                                    "region": "SEOUL",
                                                    "venue": "잠실올림픽주경기장"
                                                  }
                                                ],
                                                "hasNext": true,
                                                "size": 5,
                                                "numberOfElements": 2,
                                                "nextCursor": "eyJpZCI6MTksInN0YXJ0RGF0ZSI6IjIwMjYtMDItMjgifQ=="
                                              },
                                              "error": null
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping
    public ApiResponse<SliceResponse<ShowResponse>> getShowsPage(
            @ParameterObject
            final ShowSearchParam param,

            @Parameter(description = "한 번에 조회할 개수 (기본값: 5, 최대: 100)", example = "5")
            @RequestParam(defaultValue = "5") final int size,

            @Parameter(description = "정렬 기준 [popular(인기순), latest(최신순), approaching(공연임박순)]", example = "popular")
            @RequestParam(defaultValue = "popular") final String sort
    ) {
        final GetShowsUseCase.Input input = new GetShowsUseCase.Input(param, size, sort);
        final GetShowsUseCase.Output output = getShowsUseCase.execute(input);
        return ApiResponse.success(SliceResponse.from(output.shows(), output.nextCursor()));
    }

    @Operation(
            summary = "메인 홈 최신 공연 목록 조회",
            description = "특정 카테고리의 최신 등록된 공연 10개를 조회합니다."
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
                                              "result": "SUCCESS",
                                              "data": {
                                                "shows": [
                                                  {
                                                    "id": 20,
                                                    "title": "뮤지컬 위키드",
                                                    "image": "http://example.com/image.jpg",
                                                    "startDate": "2026-03-01",
                                                    "venue": "블루스퀘어 신한카드홀",
                                                    "createdAt": "2026-01-01T10:00:00"
                                                  },
                                                  {
                                                    "id": 19,
                                                    "title": "콘서트 BTS",
                                                    "image": "http://example.com/image2.jpg",
                                                    "startDate": "2026-02-28",
                                                    "venue": "잠실올림픽주경기장",
                                                    "createdAt": "2026-01-01T09:00:00"
                                                  }
                                                ]
                                              },
                                              "error": null
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/latest")
    public ApiResponse<GetLatestShowsUseCase.Output> getLatestShows(
            @Parameter(description = "카테고리", example = "CONCERT", required = true)
            @RequestParam(defaultValue = "CONCERT") String category) {
        GetLatestShowsUseCase.Input input = new GetLatestShowsUseCase.Input(category);
        return ApiResponse.success(getLatestShowsUseCase.execute(input));
    }

    @Operation(
            summary = "메인 홈 오픈예정 공연 목록 조회",
            description = "특정 카테고리의 예매오픈마감 임박 순 공연 5개를 조회합니다."
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
                                              "result": "SUCCESS",
                                              "data": {
                                                "shows": [
                                                  {
                                                    "id": 21,
                                                    "title": "뮤지컬 시카고",
                                                    "image": "http://example.com/chicago.jpg",
                                                    "venue": "디큐브 링크아트센터",
                                                    "saleStartDate": "2026-04-01"
                                                  },
                                                  {
                                                    "id": 22,
                                                    "title": "콘서트 싸이흠뻑쇼",
                                                    "image": "http://example.com/psy.jpg",
                                                    "venue": "잠실주경기장",
                                                    "saleStartDate": "2026-04-05"
                                                  }
                                                ]
                                              },
                                              "error": null
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/sale-opening-soon")
    public ApiResponse<GetShowsOpeningSoonUseCase.Output> getShowsSaleOpeningSoon(
            @Parameter(description = "카테고리", example = "CONCERT", required = true)
            @RequestParam(defaultValue = "CONCERT") String category,
            @RequestParam(defaultValue = "5") int size) {
        GetShowsOpeningSoonUseCase.Input input = new GetShowsOpeningSoonUseCase.Input(category, size);
        return ApiResponse.success(getShowsOpeningSoonUseCase.execute(input));
    }

    @Operation(
            summary = "판매 오픈 예정 공연 목록 조회 (무한스크롤)",
            description = """
                    판매 오픈 예정 공연 목록을 커서 기반 무한스크롤로 조회합니다.
                    
                    ## 검색 조건
                    - **title**: 공연 제목 (부분 일치 검색)
                    - **saleStartDateFrom/To**: 판매 시작일 범위
                    - **saleEndDateFrom/To**: 판매 종료일 범위
                    - **category**: 카테고리 필터
                    
                    ## 정렬 옵션
                    - `saleStartDate` (기본값) - 판매 시작일 오름차순
                    - `popular` - 인기순 (조회수 높은 순)
                    
                    ## 페이지네이션
                    - 한 페이지당 16개 조회 (기본값)
                    - `cursor` 파라미터로 다음 페이지 조회
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
                                              "result": "SUCCESS",
                                              "data": {
                                                "items": [
                                                  {
                                                    "id": 21,
                                                    "title": "뮤지컬 시카고",
                                                    "image": "http://example.com/chicago.jpg",
                                                    "venue": "디큐브 링크아트센터",
                                                    "startDate": "2026-05-01",
                                                    "endDate": "2026-07-31",
                                                    "saleStartDate": "2026-04-01",
                                                    "saleEndDate": "2026-06-30"
                                                  }
                                                ],
                                                "hasNext": true,
                                                "size": 16,
                                                "numberOfElements": 16,
                                                "nextCursor": "eyJpZCI6MzYsInNhbGVTdGFydERhdGUiOiIyMDI2LTA0LTE1In0="
                                              },
                                              "error": null
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/sale-opening-soon/page")
    public ApiResponse<SliceResponse<ShowOpeningSoonDetailResponse>> getShowsSaleOpeningSoonPage(
            @ParameterObject
            final SaleOpeningSoonSearchParam param,

            @Parameter(description = "한 번에 조회할 개수 (기본값: 16)", example = "16")
            @RequestParam(defaultValue = "16") final int size,

            @Parameter(description = "정렬 기준 [saleStartDate(판매시작일순), popular(인기순)]", example = "saleStartDate")
            @RequestParam(defaultValue = "saleStartDate") final String sort
    ) {
        final var input = new GetShowsSaleOpeningSoonPageUseCase.Input(param, size, sort);
        final var output = getShowsSaleOpeningSoonPageUseCase.execute(input);
        return ApiResponse.success(SliceResponse.from(output.shows(), output.nextCursor()));
    }
}
