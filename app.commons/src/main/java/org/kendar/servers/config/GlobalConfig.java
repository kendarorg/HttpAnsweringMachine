package org.kendar.servers.config;

import org.kendar.servers.BaseJsonConfig;

@ConfigAttribute(id="global")
public class GlobalConfig extends BaseJsonConfig<GlobalConfig> {
    private String localAddress;
    private GlobalConfigLogging logging;
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
        return result;
    }
}
