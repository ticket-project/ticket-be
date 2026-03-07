package com.ticket.core.api.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
@Schema(description = "토큰 갱신 요청")
public class RefreshTokenRequest {

    @Schema(description = "리프레시 토큰", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String refreshToken;

    public RefreshTokenRequest() {
    }

}
