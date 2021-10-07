package org.kendar.servers.http.api;

import org.kendar.http.annotations.HttpMethodFilter;
import org.kendar.http.annotations.HttpTypeFilter;

public class FilterDto {
    private HttpTypeFilter type;
    private HttpMethodFilter method;

    public FilterDto(HttpTypeFilter type, HttpMethodFilter method) {

        this.type = type;
        this.method = method;
    }

    public HttpTypeFilter getType() {
        return type;
    }

    public void setType(HttpTypeFilter type) {
        this.type = type;
    }

    public HttpMethodFilter getMethod() {
        return method;
    }

    public void setMethod(HttpMethodFilter method) {
        this.method = method;
    }
}
