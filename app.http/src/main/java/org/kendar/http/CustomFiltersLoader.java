package org.kendar.http;

import org.kendar.servers.http.Request;

import java.util.List;

public interface CustomFiltersLoader {
    List<FilterDescriptor> loadFilters();

    FilterDescriptor loadFromRequest(Request req);
}
