package com.ticket.core.api.controller.response;

import com.ticket.core.enums.OrderState;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "주문/결제 화면 상세 응답")
public record OrderDetailResponse(
        @Schema(description = "주문 ID", example = "1001")
        Long orderId,

        @Schema(description = "주문 상태", example = "PENDING")
        OrderState status,

        @Schema(description = "홀드 만료 시각", example = "2026-03-11T10:15:30")
        LocalDateTime expiresAt,

        @Schema(description = "현재 기준 남은 선점 시간(초)", example = "287")
        long remainingSeconds,

        @Schema(description = "공연 정보")
        ShowInfo show,

        @Schema(description = "회차 정보")
        PerformanceInfo performance,

        @Schema(description = "예매자 정보")
        BookerInfo booker,

        @Schema(description = "금액 정보")
        PriceInfo price,

        @Schema(description = "주문 좌석 정보")
        TicketInfo tickets
) {
    public record ShowInfo(
            @Schema(description = "공연 ID", example = "10") Long showId,
            @Schema(description = "공연명", example = "미스터트롯4 전국투어 콘서트 - 서울") String title,
            @Schema(description = "공연 이미지 URL") String imageUrl
    ) {}

    public record PerformanceInfo(
            @Schema(description = "회차 ID", example = "55") Long performanceId,
            @Schema(description = "회차 번호", example = "1") Long performanceNo,
            @Schema(description = "공연 시작 시각", example = "2026-04-25T13:00:00") LocalDateTime startTime,
            @Schema(description = "공연장명", example = "장충체육관") String venueName
    ) {}

    public record BookerInfo(
            @Schema(description = "회원 ID", example = "7") Long memberId,
            @Schema(description = "예매자명", example = "홍길동") String name,
            @Schema(description = "이메일", example = "user@example.com") String email
    ) {}

    public record PriceInfo(
            @Schema(description = "티켓 금액", example = "240000") BigDecimal ticketAmount,
            @Schema(description = "예매 수수료", example = "0") BigDecimal bookingFee,
            @Schema(description = "배송비", example = "0") BigDecimal deliveryFee,
            @Schema(description = "할인 금액", example = "0") BigDecimal discountAmount,
            @Schema(description = "최종 결제 금액", example = "240000") BigDecimal totalAmount
    ) {}

    public record TicketInfo(
            @Schema(description = "티켓 수량", example = "2") int count,
            @Schema(description = "주문 좌석 목록") List<TicketSeat> seats
    ) {}

    public record TicketSeat(
            @Schema(description = "회차 좌석 ID", example = "501") Long performanceSeatId,
            @Schema(description = "좌석 ID", example = "42") Long seatId,
            @Schema(description = "층", example = "1") int floor,
            @Schema(description = "구역", example = "A") String section,
            @Schema(description = "행", example = "10") String rowNo,
            @Schema(description = "열", example = "7") String seatNo,
            @Schema(description = "표시용 좌석명", example = "1F A구역 10열 7번") String label,
            @Schema(description = "좌석 가격", example = "120000") BigDecimal price
    ) {}
}
