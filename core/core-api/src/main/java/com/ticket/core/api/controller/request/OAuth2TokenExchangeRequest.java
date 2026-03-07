package com.ticket.core.api.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
@Schema(description = "OAuth2 인증 코드 토큰 교환 요청")
public class OAuth2TokenExchangeRequest {

    @Schema(description = "서버에서 발급한 1회용 인증 코드", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String code;

    public OAuth2TokenExchangeRequest() {
    }

}
