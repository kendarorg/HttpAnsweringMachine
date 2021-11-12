package org.kendar.servers.config;

@ConfigAttribute(id="https")
public class HttpsWebServerConfig extends WebServerConfig{
  @Override protected WebServerConfig newInstance() {
    return new HttpsWebServerConfig();
  }
}
