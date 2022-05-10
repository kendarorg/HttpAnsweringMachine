package org.kendar.oidc;

import org.kendar.servers.BaseJsonConfig;
import org.kendar.servers.config.ConfigAttribute;

@ConfigAttribute(id="oidc.server")
public class OidcConfig extends BaseJsonConfig<OidcConfig> {
  private int tokenExpiration;

  @Override public OidcConfig copy() {
    var result = new OidcConfig();
    result.tokenExpiration = this.tokenExpiration;
    return result;
  }

  public int getTokenExpiration() {
    return tokenExpiration;
  }

  public void setTokenExpiration(int tokenExpiration) {
    this.tokenExpiration = tokenExpiration;
  }
}
