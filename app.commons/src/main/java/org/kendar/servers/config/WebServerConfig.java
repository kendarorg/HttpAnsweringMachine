package org.kendar.servers.config;

public class WebServerConfig {
    private boolean active;
    private int port;
    private int backlog;
    private boolean useCachedExecutor;

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
}
