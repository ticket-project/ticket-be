package com.ticket.core.api.controller.request;

public class ShowSearchParam {
    private String category;
    private String place;

    public ShowSearchParam(final String category, final String place) {
        this.category = category;
        this.place = place;
    }

    public String getCategory() {
        return category;
    }

    public String getPlace() {
        return place;
    }
}
