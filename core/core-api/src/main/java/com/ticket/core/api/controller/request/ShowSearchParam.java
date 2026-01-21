package com.ticket.core.api.controller.request;

public class ShowSearchParam {
    private String category;

    public ShowSearchParam(final String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }
}
