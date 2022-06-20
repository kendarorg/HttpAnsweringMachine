package org.kendar.servers.config;

@ConfigAttribute(id="http")
public class HttpWebServerConfig extends WebServerConfig{
  @Override protected WebServerConfig newInstance() {
    return new HttpWebServerConfig();
  }
}
