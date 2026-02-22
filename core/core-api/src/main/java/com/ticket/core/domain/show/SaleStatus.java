package com.ticket.core.domain.show;

import java.time.LocalDate;

/**
 * 판매 상태를 나타내는 Enum
 * - UPCOMING: 판매예정 (saleStartDate > today)
 * - ON_SALE: 판매중 (saleStartDate <= today <= saleEndDate)
 * - CLOSED: 판매종료 (saleEndDate < today)
 */
public enum SaleStatus {
    UPCOMING("판매예정"),
    ON_SALE("판매중"),
    CLOSED("판매종료");

    private final String description;

    SaleStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getCode() {
        return name();
    }

    /**
     * 판매 시작일과 종료일을 기준으로 현재 판매 상태를 계산
     */
    public static SaleStatus calculate(LocalDate saleStartDate, LocalDate saleEndDate) {
        LocalDate today = LocalDate.now();
        
        if (saleStartDate == null || saleEndDate == null) {
            return null;
        }
        
        if (today.isBefore(saleStartDate)) {
            return UPCOMING;
        } else if (today.isAfter(saleEndDate)) {
            return CLOSED;
        } else {
            return ON_SALE;
        }
    }
}
