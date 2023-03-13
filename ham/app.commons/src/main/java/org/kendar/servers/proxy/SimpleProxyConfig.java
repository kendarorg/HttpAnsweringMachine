package org.kendar.servers.proxy;

import org.kendar.servers.BaseJsonConfig;
import org.kendar.servers.config.ConfigAttribute;

import java.util.ArrayList;
import java.util.List;

@ConfigAttribute(id = "proxy")
public class SimpleProxyConfig extends BaseJsonConfig<SimpleProxyConfig> {
    private List<RemoteServerStatus> proxies = new ArrayList<>();

    @Override
    public boolean isSystem() {
        return true;
    }

    @Override
    public SimpleProxyConfig copy() {
        var result = new SimpleProxyConfig();
        result.setProxies(new ArrayList<>());
        for (var rss : proxies) {
            result.getProxies().add(rss.copy());
        }
        result.setId(this.getId());
        result.setSystem(this.isSystem());
        return result;
    }

    public List<RemoteServerStatus> getProxies() {
        return proxies;
    }

    public void setProxies(List<RemoteServerStatus> proxies) {
        for (var proxy : proxies) {
            proxy.setRunning(false);
        }
        this.proxies = proxies;
    }
}
