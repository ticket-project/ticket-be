package com.ticket.core.domain.show;

import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;

public enum ShowSortKey {
    POPULAR("popular"),          // 기본: 인기순
    LATEST("latest"),            // 최신순
    SHOW_APPROACHING("approaching"),   // 공연 임박순
    SALE_OPENING("saleStartDate");    // 판매 오픈 예정순 (판매시작일 오름차순)

    private final String apiValue;

    ShowSortKey(String apiValue) {
        this.apiValue = apiValue;
    }

    public String getApiValue() {
        return apiValue;
    }

    public static ShowSortKey fromApiValue(String v) {
        if (v == null || v.isBlank()) return POPULAR;
        for (var k : values()) {
            if (k.apiValue.equalsIgnoreCase(v)) return k;
        }
        throw new CoreException(ErrorType.NOT_SUPPORT_SHOW_SORT, "지원하지 않는 sort: " + v);
    }
}
