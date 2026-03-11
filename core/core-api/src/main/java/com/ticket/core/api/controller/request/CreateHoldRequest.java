package com.ticket.core.api.controller.request;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;

@Getter
@Schema(description = "좌석 HOLD 생성 요청")
public class CreateHoldRequest {

    @NotEmpty(message = "seatIds는 비어 있을 수 없습니다.")
    @Size(max = 4, message = "한 번에 최대 4석까지만 선점할 수 있습니다.")
    @ArraySchema(schema = @Schema(description = "좌석 ID", example = "42"))
    private List<Long> seatIds;

    public CreateHoldRequest() {
    }
}
