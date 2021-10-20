package org.kendar.dns.configurations;

import java.util.List;

public class DnsConfig {
    private boolean active;
    private int port;
    private List<ExtraDnsServer> extraServers;
    private List<String> blocked;
    private List<ResolvedDns> resolved;

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

    public List<ExtraDnsServer> getExtraServers() {
        return extraServers;
    }

    public void setExtraServers(List<ExtraDnsServer> extraServers) {
        this.extraServers = extraServers;
    }

    public List<String> getBlocked() {
        return blocked;
    }

    public void setBlocked(List<String> blocked) {
        this.blocked = blocked;
    }

    public List<ResolvedDns> getResolved() {
        return resolved;
    }

    public void setResolved(List<ResolvedDns> resolved) {
        this.resolved = resolved;
    }
}
