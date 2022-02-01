package org.kendar.servers.config;

import org.kendar.servers.BaseJsonConfig;

public abstract class WebServerConfig extends BaseJsonConfig<WebServerConfig> {
    private boolean active;
    private int port;
    private int backlog;
    private boolean useCachedExecutor;

    @Override
    public boolean isSystem() {
        return true;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getBacklog() {
        return backlog;
    }

    public void setBacklog(int backlog) {
        this.backlog = backlog;
    }

    public boolean isUseCachedExecutor() {
        return useCachedExecutor;
    }

    public void setUseCachedExecutor(boolean useCachedExecutor) {
        this.useCachedExecutor = useCachedExecutor;
    }

    @Override
    public WebServerConfig copy() {
        var result = newInstance();
        result.active = this.active;
        result.port = this.port;
        result.backlog = this.backlog;
        result.useCachedExecutor = this.useCachedExecutor;
        return result;
    }

    protected abstract WebServerConfig newInstance();
}
