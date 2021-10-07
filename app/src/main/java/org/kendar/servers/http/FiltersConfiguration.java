package org.kendar.servers.http;

import org.kendar.http.FilterDescriptor;
import org.kendar.http.HttpFilterType;

import java.util.HashMap;
import java.util.List;

public class FiltersConfiguration {
    public HashMap<HttpFilterType, List<FilterDescriptor>> filters = new HashMap<>();
    public HashMap<String,FilterDescriptor> filtersById = new HashMap<>();
}
