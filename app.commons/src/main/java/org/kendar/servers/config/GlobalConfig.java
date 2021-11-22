package org.kendar.servers.config;

import org.kendar.servers.BaseJsonConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ConfigAttribute(id = "global")
public class GlobalConfig extends BaseJsonConfig<GlobalConfig> {
  private String localAddress;
  private GlobalConfigLogging logging;
  private ConcurrentHashMap<String, Boolean> filters = new ConcurrentHashMap<>();

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

  @Override
  public GlobalConfig copy() {
    var result = new GlobalConfig();
    result.localAddress = this.localAddress;
    result.logging = this.logging.copy();
    result.filters = new ConcurrentHashMap<>();
    for (var filter : this.filters.entrySet()) {
      result.filters.put(filter.getKey(), filter.getValue());
    }
    return result;
  }

  public Map<String, Boolean> getFilters() {
    return filters;
  }

  public void setFilters(Map<String, Boolean> filters) {
    this.filters = new ConcurrentHashMap<>(filters);
  }

  public boolean checkFilterEnabled(String key) {
    if (!filters.containsKey(key)) return true;
    return filters.get(key);
  }
}
