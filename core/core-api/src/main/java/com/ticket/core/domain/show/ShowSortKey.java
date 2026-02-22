package com.ticket.core.domain.show;

import com.ticket.core.support.exception.CoreException;
import com.ticket.core.support.exception.ErrorType;

public enum ShowSortKey {
    POPULAR("popular", "인기순"),
    LATEST("latest", "최신순"),
    SHOW_START_APPROACHING("showStartApproaching", "공연 임박순"),
    SALE_START_APPROACHING("saleStartApproaching", "판매 오픈 임박순");

    private final String apiValue;
    private final String description;

    ShowSortKey(final String apiValue, final String description) {
        this.apiValue = apiValue;
        this.description = description;
    }

    public String getApiValue() {
        return apiValue;
    }

    public String getDescription() {
        return description;
    }

    public static ShowSortKey fromApiValue(String v) {
        if (v == null || v.isBlank()) return POPULAR;
        for (var k : values()) {
            if (k.apiValue.equalsIgnoreCase(v)) return k;
        }
        throw new CoreException(ErrorType.NOT_SUPPORT_SHOW_SORT, "지원하지 않는 sort: " + v);
    }
}
