package org.kendar.servers.http;

import org.kendar.servers.BaseJsonConfig;

public class JsFilterConfig extends BaseJsonConfig<JsFilterConfig> {
  private String path;

  @Override public JsFilterConfig copy() {
    var result = new JsFilterConfig();
    result.path = this.path;
    return result;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }
}
