package org.kendar.http;

import java.util.List;

public interface CustomFiltersLoader {
    List<FilterDescriptor> loadFilters();
    FilterDescriptor loadFilterFile(String fileName, byte[] fileData, boolean overwrite);
}
