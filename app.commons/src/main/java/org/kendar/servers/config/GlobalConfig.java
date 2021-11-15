package org.kendar.servers.config;

import org.kendar.servers.BaseJsonConfig;

import java.util.ArrayList;
import java.util.List;

@ConfigAttribute(id="global")
public class GlobalConfig extends BaseJsonConfig<GlobalConfig> {
    private String localAddress;
    private GlobalConfigLogging logging;
    private List<FilterStatus> filters = new ArrayList<>();
    @Override
    public boolean isSystem() {
        return true;
    }

    public String getLocalAddress() {
        return localAddress;
    }

    public void setLocalAddress(String localAddress) {
        this.localAddress = localAddress;
    }

    public GlobalConfigLogging getLogging() {
        return logging;
    }

    public void setLogging(GlobalConfigLogging logging) {
        this.logging = logging;
    }

    @Override public GlobalConfig copy() {
        var result = new GlobalConfig();
        result.localAddress = this.localAddress;
        result.logging = this.logging.copy();
        result.filters = new ArrayList<>();
        for (var filter : this.filters) {
            result.filters.add(filter.copy());
        }
        return result;
    }

    public List<FilterStatus> getFilters() {
        return filters;
    }

    public void setFilters(List<FilterStatus> filters) {
        this.filters = filters;
    }
}
