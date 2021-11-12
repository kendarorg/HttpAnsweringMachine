package org.kendar.servers.config;

import org.kendar.servers.Copyable;

public class FilterStatus implements Copyable<FilterStatus> {
  private String id;
  private boolean enabled;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public FilterStatus copy() {
    var result = new FilterStatus();
    result.id = this.id;
    result.enabled = this.enabled;
    return result;
  }
}
