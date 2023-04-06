package org.kendar.mongo.config;

import org.kendar.servers.BaseJsonConfig;
import org.kendar.servers.SpecialJsonConfig;
import org.kendar.servers.config.ConfigAttribute;

import java.util.ArrayList;
import java.util.List;


@ConfigAttribute(id = "mongo")
public class MongoConfig extends BaseJsonConfig<MongoConfig> implements SpecialJsonConfig {
    private boolean active;

    public List<MongoProxy> getProxies() {
        return proxies;
    }

    public void setProxies(List<MongoProxy> proxies) {
        this.proxies = proxies;
    }

    private List<MongoProxy> proxies = new ArrayList<>();
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public MongoConfig copy() {
        var result = new MongoConfig();
        result.active = this.active;
        result.setProxies(new ArrayList<>());
        for (var rss : proxies) {
            result.getProxies().add(rss.copy());
        }
        result.setId(this.getId());
        return result;
    }

    @Override
    public void preSave() {

    }
}
