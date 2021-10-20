package org.kendar.servers.config;

public class GlobalConfig {
    private String localAddress;
    private GlobalConfigLogging logging;

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
}
