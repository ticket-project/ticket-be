package com.ticket.core.api.controller;

import com.ticket.core.api.controller.response.MetaCodesResponse;
import com.ticket.core.domain.commoncode.usecase.GetMetaCodesUseCase;
import com.ticket.core.support.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/meta")
@Tag(name = "Meta", description = "프론트 공통 코드/메타 정보 API")
@RequiredArgsConstructor
public class MetaController {

    private final GetMetaCodesUseCase getMetaCodesUseCase;

    @GetMapping("/codes")
    @Operation(
            summary = "공통 코드 조회",
            description = "프론트에서 필요한 카테고리/장르/Enum 값들을 한 번에 조회합니다."
    )
    public ApiResponse<MetaCodesResponse> getMetaCodes() {
        final GetMetaCodesUseCase.Output output = getMetaCodesUseCase.execute();
        return ApiResponse.success(output.codes());
    }
}
