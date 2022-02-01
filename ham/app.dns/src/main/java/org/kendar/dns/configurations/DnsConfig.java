package org.kendar.dns.configurations;

import org.kendar.servers.BaseJsonConfig;
import org.kendar.servers.SpecialJsonConfig;
import org.kendar.servers.config.ConfigAttribute;

import java.util.ArrayList;
import java.util.List;

@ConfigAttribute(id="dns")
public class DnsConfig extends BaseJsonConfig<DnsConfig> implements SpecialJsonConfig {
    private boolean active;
    private int port;
    private List<ExtraDnsServer> extraServers;
    private List<String> blocked;
    private List<PatternItem> resolved;

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

    public List<PatternItem> getResolved() {
        return resolved;
    }

    public void setResolved(List<PatternItem> resolved) {
        this.resolved = resolved;
    }

    @Override public DnsConfig copy() {
        var result = new DnsConfig();
        result.active =this.active;
        result.blocked = new ArrayList<>( this.blocked);
        result.port = this.port;
        result.resolved = new ArrayList<>();
        for (int i = 0; i < this.resolved.size(); i++) {
            var resol = this.resolved.get(i);
            result.resolved.add(resol.copy());
        }
        result.extraServers = new ArrayList<>();
        for (int i = 0; i < this.extraServers.size(); i++) {
            var ext = this.extraServers.get(i);
            result.extraServers.add(ext.copy());
        }
        return result;
    }

    @Override public void preSave() {
        var newExtraServers = new ArrayList<ExtraDnsServer>();
        for (int i = 0; i < this.extraServers.size(); i++) {
            var ext = this.extraServers.get(i);
            if(ext.isEnv())continue;
            newExtraServers.add(ext);
        }
        this.extraServers = newExtraServers;
    }
}
