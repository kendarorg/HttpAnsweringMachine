package org.kendar.servers.http.configurations;

import org.kendar.http.FilterDescriptor;
import org.kendar.http.HttpFilterType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FiltersConfiguration {
    public final HashMap<HttpFilterType, List<FilterDescriptor>> filters = new HashMap<>();
    public final HashMap<String,FilterDescriptor> filtersById = new HashMap<>();
    public final HashMap<String,List<FilterDescriptor>> filtersByClass = new HashMap<>();

    public FiltersConfiguration copy() {
        var result = new FiltersConfiguration();
        for (var item :filters.entrySet()) {
            result.filters.put(item.getKey(),new ArrayList<>(item.getValue()));
        }
        result.filtersById.putAll(filtersById);
        for (var item :filtersByClass.entrySet()) {
            result.filtersByClass.put(item.getKey(),new ArrayList<>(item.getValue()));
        }
        return result;
    }
}
