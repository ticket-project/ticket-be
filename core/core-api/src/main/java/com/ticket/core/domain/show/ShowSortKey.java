package com.ticket.core.domain.show;

import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;

public enum ShowSortKey {
    POPULAR("popular"),          // 기본: 인기순
    LATEST("latest"),            // 최신순
    ENDING_SOON("endingSoon");   // 마감 임박순

    private final String apiValue;

    ShowSortKey(String apiValue) { this.apiValue = apiValue; }

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
