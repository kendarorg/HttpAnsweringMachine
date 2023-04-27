package org.kendar.servers.dbproxy;

import org.kendar.janus.engine.Engine;

public class ServerData {
    private boolean active;
    private Engine serverEngine;
    private DbDescriptor local;

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public DbDescriptor getLocal() {
        return local;
    }

    public void setLocal(DbDescriptor local) {
        this.local = local;
    }

    public Engine getServerEngine() {
        return serverEngine;
    }

    public void setServerEngine(Engine serverEngine) {
        this.serverEngine = serverEngine;
    }
}
