package org.kendar.servers.dbproxy;

import org.kendar.servers.BaseJsonConfig;
import org.kendar.servers.config.ConfigAttribute;

import java.util.ArrayList;
import java.util.List;

@ConfigAttribute(id = "dbproxy")
public class DbProxyConfig extends BaseJsonConfig<DbProxyConfig> {
    @Override
    public boolean isSystem() {
        return true;
    }

    private List<DbProxy> proxies = new ArrayList<>();

    public List<DbProxy> getProxies() {
        return proxies;
    }

    public void setProxies(List<DbProxy> proxies) {
        this.proxies = proxies;
    }

    @Override
    public DbProxyConfig copy() {
        var result = new DbProxyConfig();
        result.setProxies(new ArrayList<>());
        for (var rss : proxies) {
            result.getProxies().add(rss.copy());
        }
        result.setId(this.getId());
        result.setSystem(this.isSystem());
        return result;
    }
}
