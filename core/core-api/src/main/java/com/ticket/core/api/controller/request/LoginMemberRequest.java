package com.ticket.core.api.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Schema(description = "로그인 요청")
@Getter
public class LoginMemberRequest {

    @Schema(description = "이메일 주소", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private final String email;

    @Schema(description = "비밀번호", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private final String password;

    public LoginMemberRequest(final String email, final String password) {
        this.email = email;
        this.password = password;
    }
}

