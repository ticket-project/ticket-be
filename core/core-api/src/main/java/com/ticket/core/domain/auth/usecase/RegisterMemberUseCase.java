package com.ticket.core.domain.auth.usecase;

import com.ticket.core.domain.auth.AuthService;
import com.ticket.core.domain.member.vo.Email;
import com.ticket.core.domain.member.vo.RawPassword;
import com.ticket.core.enums.Role;
import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegisterMemberUseCase {

    private final AuthService authService;

    public record Input(
            @Schema(description = "로그인 아이디(이메일)", example = "user@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
            @JsonAlias({"id", "loginId"})
            @NotBlank
            String email,

            @Schema(description = "비밀번호", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotBlank
            String password,

            @Schema(description = "회원 이름", example = "홍길동", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotBlank
            String name
    ) {}
    public record Output(Long memberId) {}

    public Output execute(final Input input) {
        return new Output(authService.register(
                Email.create(input.email()),
                RawPassword.create(input.password()),
                input.name(),
                Role.MEMBER
        ));
    }
}
