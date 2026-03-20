package com.prevelio.customer.application.dto;

import jakarta.ws.rs.QueryParam;

public record CustomerSearchCriteria(
    @QueryParam("name") String name,
    @QueryParam("email") String email,
    @QueryParam("pageIndex") Integer pageIndex,
    @QueryParam("pageSize") Integer pageSize
) {
    public int getPageIndexOrDefault() {
        return pageIndex != null ? pageIndex : 0;
    }

    public int getPageSizeOrDefault() {
        return pageSize != null ? pageSize : 10;
    }
}
