package com.prevelio.customer.application.dto;

import jakarta.ws.rs.QueryParam;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerSearchCriteria {
    
    @QueryParam("name")
    private String name;

    @QueryParam("email")
    private String email;

    @QueryParam("pageIndex")
    private Integer pageIndex;

    @QueryParam("pageSize")
    private Integer pageSize;

    public int getPageIndexOrDefault() {
        return pageIndex != null ? pageIndex : 0;
    }

    public int getPageSizeOrDefault() {
        return pageSize != null ? pageSize : 10;
    }
}
