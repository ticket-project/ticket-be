package com.ticket.core.api.controller.request;

public class ShowSearchParam {
    private String category;
    private String place;

    private String cursor;

    public ShowSearchParam(final String category, final String place, final String cursor) {
        this.category = category;
        this.place = place;
        this.cursor = cursor;
    }

    public String getCategory() {
        return category;
    }

    public String getPlace() {
        return place;
    }

    public String getCursor() {
        return cursor;
    }
}
