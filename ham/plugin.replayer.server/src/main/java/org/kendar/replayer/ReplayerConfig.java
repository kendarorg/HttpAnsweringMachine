package org.kendar.replayer;

import org.kendar.servers.BaseJsonConfig;
import org.kendar.servers.config.ConfigAttribute;

@ConfigAttribute(id="replayer.server")
public class ReplayerConfig extends BaseJsonConfig<ReplayerConfig> {
  private String path;

  @Override public ReplayerConfig copy() {
    var result = new ReplayerConfig();
    result.setId(this.getId());
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
