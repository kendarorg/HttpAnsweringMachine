package org.kendar.servers.http.configurations;

import org.kendar.http.FilterDescriptor;
import org.kendar.http.HttpFilterType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FiltersConfiguration {
    public HashMap<HttpFilterType, List<FilterDescriptor>> filters = new HashMap<>();
    public HashMap<String,FilterDescriptor> filtersById = new HashMap<>();

    public FiltersConfiguration copy() {
        var result = new FiltersConfiguration();
        for (var item :filters.entrySet()) {
            result.filters.put(item.getKey(),new ArrayList<>(item.getValue()));
        }
        for (var item :filtersById.entrySet()) {
            result.filtersById.put(item.getKey(),item.getValue());
        }
        return result;
    }
}
