package org.kendar.socks5;

import org.kendar.servers.BaseJsonConfig;
import org.kendar.servers.config.ConfigAttribute;

@ConfigAttribute(id="socks5.server")
public class Socks5Config extends BaseJsonConfig<Socks5Config> {
  private int port;
  private int httpProxyPort;
  private boolean active = false;

  @Override public Socks5Config copy() {
    var result = new Socks5Config();
    result.setId(this.getId());
    result.setPort(this.getPort());
    result.setActive(this.active);
    result.setHttpProxyPort(this.httpProxyPort);
    return result;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public int getHttpProxyPort() {
    return httpProxyPort;
  }

  public void setHttpProxyPort(int httpProxyPort) {
    this.httpProxyPort = httpProxyPort;
  }
}
