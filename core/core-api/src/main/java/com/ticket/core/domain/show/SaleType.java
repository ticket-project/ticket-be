package com.ticket.core.domain.show;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum SaleType {
    GENERAL("일반판매"),
    EXCLUSIVE("단독판매");

    private final String description;

    SaleType(String description) {
        this.description = description;
    }

    @JsonProperty("code")
    public String getCode() {
        return name();
    }

    @JsonProperty("name")
    public String getDescription() {
        return description;
    }
}