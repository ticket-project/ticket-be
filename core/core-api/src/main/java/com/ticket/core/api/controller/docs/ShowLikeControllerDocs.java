package com.ticket.core.api.controller.docs;

import com.ticket.core.api.controller.response.ShowLikeStatusResponse;
import com.ticket.core.api.controller.response.ShowLikeSummaryResponse;
import com.ticket.core.domain.member.MemberPrincipal;
import com.ticket.core.support.response.ApiResponse;
import com.ticket.core.support.response.SliceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Show Like", description = "Show like APIs")
public interface ShowLikeControllerDocs {

    @Operation(
            summary = "Add show like",
            description = "Adds a like for the authenticated member. If already liked, returns success idempotently."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Like added"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Show not found")
    })
    ApiResponse<ShowLikeStatusResponse> likeShow(
            @Parameter(hidden = true) MemberPrincipal memberPrincipal,
            @Parameter(description = "Show ID", example = "1", required = true) Long showId
    );

    @Operation(
            summary = "Remove show like",
            description = "Removes a like for the authenticated member. If not liked, returns success idempotently."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Like removed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Show not found")
    })
    ApiResponse<ShowLikeStatusResponse> unlikeShow(
            @Parameter(hidden = true) MemberPrincipal memberPrincipal,
            @Parameter(description = "Show ID", example = "1", required = true) Long showId
    );

    @Operation(
            summary = "Get show like status",
            description = "Returns whether the authenticated member liked the show."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Status fetched"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Show not found")
    })
    ApiResponse<ShowLikeStatusResponse> getLikeStatus(
            @Parameter(hidden = true) MemberPrincipal memberPrincipal,
            @Parameter(description = "Show ID", example = "1", required = true) Long showId
    );

    @Operation(
            summary = "Get my liked shows",
            description = "Returns liked shows for the authenticated member with cursor pagination."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Liked shows fetched"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    ApiResponse<SliceResponse<ShowLikeSummaryResponse>> getMyLikes(
            @Parameter(hidden = true) MemberPrincipal memberPrincipal,
            @Parameter(description = "Cursor (last like id)", example = "123") String cursor,
            @Parameter(description = "Page size", example = "20") int size
    );
}
